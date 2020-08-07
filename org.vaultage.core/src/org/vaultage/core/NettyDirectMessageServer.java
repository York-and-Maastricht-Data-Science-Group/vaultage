package org.vaultage.core;

import java.io.IOException;
import java.net.InetSocketAddress;

import org.vaultage.core.VaultageMessage.MessageType;
import org.vaultage.util.VaultageEncryption;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.ReferenceCountUtil;

public class NettyDirectMessageServer extends Thread implements DirectMessageServer {

	private EventLoopGroup bossGroup;
	private EventLoopGroup workerGroup;
	private ServerBootstrap serverBootstrap;
	private ChannelFuture channelFuture;
	private InetSocketAddress localAddress;

	private Vaultage vaultage;
	private String privateKey;

	public static void main(String[] args) {
		try {
			System.out.println("Starting Socket Server ..");
			DirectMessageServer server = new NettyDirectMessageServer(new InetSocketAddress("0.0.0.0", 50000), null);
			server.start();
			System.out.println("Server started at " + server.getLocalAddress() + ":" + server.getLocalPort());

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public NettyDirectMessageServer(String address, int port, Vaultage vaultage) throws IOException {
		localAddress = new InetSocketAddress(address, port);
		this.vaultage = vaultage;
	}

	public NettyDirectMessageServer(InetSocketAddress address, Vaultage vaultage) throws IOException {
		localAddress = address;
		this.vaultage = vaultage;
	}

	public String getLocalAddress() {
		return localAddress.getAddress().getHostAddress();
	}

	public int getLocalPort() {
		return localAddress.getPort();
	}

	@Override
	public void run() {
		bossGroup = new NioEventLoopGroup();
		workerGroup = new NioEventLoopGroup();
		try {
			serverBootstrap = new ServerBootstrap();
			serverBootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
					.childHandler(new ChannelInitializer<SocketChannel>() {
						@Override
						public void initChannel(SocketChannel ch) throws Exception {
//							ch.pipeline().addLast(new DelimiterBasedFrameDecoder(8192, Delimiters.lineDelimiter()));
							ch.pipeline().addLast(new LineBasedFrameDecoder(Integer.MAX_VALUE));
							ch.pipeline().addLast(new StringDecoder());
							ch.pipeline().addLast(new StringEncoder());
							ch.pipeline().addLast(new NettyServerHandler());
						}
					}).option(ChannelOption.SO_BACKLOG, 128).childOption(ChannelOption.SO_KEEPALIVE, true);

			channelFuture = serverBootstrap.bind(localAddress).sync();

//			channelFuture.channel().closeFuture().sync();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
//			workerGroup.shutdownGracefully();
//			bossGroup.shutdownGracefully();
		}
	}

	public void shutdown() throws IOException, InterruptedException {
		channelFuture.channel().closeFuture();
		workerGroup.shutdownGracefully();
		bossGroup.shutdownGracefully();
	}

	public class NettyServerHandler extends ChannelInboundHandlerAdapter {

		@Override
		public void channelRead(ChannelHandlerContext ctx, Object message) throws Exception {
			String mergedMessage = (String) message;
//			ByteBuf in = (ByteBuf) msg;
			try {
//				while (textMessage.isReadable()) {
//					System.out.print((char) textMessage.readByte());
//					System.out.flush();
//				}
//				System.out.println(mergedMessage);

				String senderPublicKey = mergedMessage.substring(0, VaultageEncryption.PUBLIC_KEY_LENGTH);
				String encryptedMessage = mergedMessage.substring(VaultageEncryption.PUBLIC_KEY_LENGTH,
						mergedMessage.length());

//				System.out.println(senderPublicKey);
//				System.out.println(encryptedMessage);

				if (vaultage != null) {
					String content = VaultageEncryption.doubleDecrypt(encryptedMessage, senderPublicKey,
							NettyDirectMessageServer.this.privateKey);

					// System.out.println("RECEIVED MESSAGE: " + topicId + "\n" + content);

					VaultageMessage vaultageMessage = Vaultage.deserialise(content, VaultageMessage.class);
					MessageType msgType = vaultageMessage.getMessageType();

					switch (msgType) {
					case REQUEST:
						// calls the user vault method associated with the operation
						vaultage.getRequestMessageHandler().process(vaultageMessage, senderPublicKey,
								vaultage.getVault());
						break;
					case RESPONSE:
						// calls the registered handler of the operation
						vaultage.getResponseMessageHandler().process(vaultageMessage, senderPublicKey,
								vaultage.getVault());
					}
				}

			} finally {
				ReferenceCountUtil.release(message);
			}
		}

		@Override
		public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
			cause.printStackTrace();
			ctx.close();
		}

	}

	@Override
	public void setPrivateKey(String privateKey) {
		this.privateKey = privateKey;
	};
}
