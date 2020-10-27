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

/***
 * The Netty implementation of direct message server. The class extends class
 * Thread so that it doesn't block other processes and runs in background.
 * 
 * @author Alfa Yohannis
 *
 */
public class NettyDirectMessageServer extends Thread implements DirectMessageServer {

	private EventLoopGroup bossGroup;
	private EventLoopGroup workerGroup;
	private ServerBootstrap serverBootstrap;
	private ChannelFuture channelFuture;
	private InetSocketAddress localAddress;

	private Vaultage vaultage;
	private String privateKey;

	/***
	 * For testing or dummy of this Netty direct message server.
	 * 
	 * @param args
	 */
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

	/***
	 * The constructor of this implementation of direct message server.
	 * 
	 * @param address  the ip or hostname of the direct message server
	 * @param port     the port of the direct message server
	 * @param vaultage the Vaultage of the direct message server
	 * @throws IOException
	 */
	public NettyDirectMessageServer(String address, int port, Vaultage vaultage) throws IOException {
		localAddress = new InetSocketAddress(address, port);
		this.vaultage = vaultage;
	}

	/***
	 * The constructor of this implementation of direct message server.
	 * 
	 * @param address  the ip or hostname and port of the direct message server
	 * @param vaultage the Vaultage of this direct message server
	 * @throws IOException
	 */
	public NettyDirectMessageServer(InetSocketAddress address, Vaultage vaultage) throws IOException {
		localAddress = address;
		this.vaultage = vaultage;
	}

	/***
	 * Get the ip address or hostname of the direct message server.
	 * 
	 * @return ip or hostname
	 */
	public String getLocalAddress() {
		return localAddress.getAddress().getHostAddress();
	}

	/***
	 * Get the port of the direct message server.
	 * 
	 * @return port number
	 */
	public int getLocalPort() {
		return localAddress.getPort();
	}

	/***
	 * The process when this direct message server started is defined here.
	 */
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
							/**
							 * TODO This line based frame decoder can be a problem when the number of
							 * characters in a message is longer than Integer.MAX_VALUE. A custom line-based
							 * frame decoder shoud be defined.
							 **/
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

	/***
	 * To shutdown the direct message server.
	 * 
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public void shutdown() throws IOException, InterruptedException {
		channelFuture.channel().closeFuture();
		workerGroup.shutdownGracefully();
		bossGroup.shutdownGracefully();
	}

	/***
	 * The class handler for a received message. This is where we define how to
	 * respond to a message received.
	 * 
	 * @author Alfa Yohannis
	 *
	 */
	public class NettyServerHandler extends ChannelInboundHandlerAdapter {

		/***
		 * The method that handles when a message is received.
		 * 
		 * @param ctx     the context
		 * @param message the message
		 * 
		 */
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

				String encryptionFlag = mergedMessage.substring(0, 1);
				String senderPublicKey = mergedMessage.substring(1, 1 + VaultageEncryption.PUBLIC_KEY_LENGTH);
				String encryptedMessage = mergedMessage.substring(1 + VaultageEncryption.PUBLIC_KEY_LENGTH,
						mergedMessage.length());

//				System.out.println(senderPublicKey);
//				System.out.println(encryptedMessage);

				if (vaultage != null) {
					String content = (encryptionFlag.equals("1")) ? VaultageEncryption.doubleDecrypt(
							encryptedMessage, senderPublicKey, NettyDirectMessageServer.this.privateKey) : encryptedMessage;


					// System.out.println("RECEIVED MESSAGE: " + topicId + "\n" + content);

					VaultageMessage vaultageMessage = Vaultage.deserialise(content, VaultageMessage.class);
					MessageType msgType = vaultageMessage.getMessageType();

					vaultage.getPublicKeyToRemoteAddress().put(senderPublicKey,
							new InetSocketAddress(vaultageMessage.getSenderAddress(), vaultageMessage.getSenderPort()));

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

		/***
		 * What should be done when an exception is caught.
		 */
		@Override
		public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
			cause.printStackTrace();
			ctx.close();
		}

	}

	/***
	 * To set the private key of the direct message server. It is used to decrypt
	 * received messages.
	 * 
	 * @param privateKey
	 */
	@Override
	public void setPrivateKey(String privateKey) {
		this.privateKey = privateKey;
	};
}
