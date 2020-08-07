package org.vaultage.core;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.UUID;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

public class NettyDirectMessageClient implements DirectMessageClient {

	String serverName;
	int port;
	private InetSocketAddress address;
	private ChannelFuture channelFuture;
	private EventLoopGroup workerGroup;
	private Bootstrap bootstrap;

	public static void main(String[] args) throws InterruptedException {
		try {
			DirectMessageClient client = new NettyDirectMessageClient("localhost", 9999);
//			DirectMessageClient client = new NettyDirectMessageClient("192.168.56.101", 9998);
			client.connect();
			client.sendMessage(UUID.randomUUID().toString());
			client.disconnect();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public NettyDirectMessageClient(InetSocketAddress address) throws UnknownHostException, IOException {
		this(address.getAddress().getHostAddress(), address.getPort());
	}

	public InetSocketAddress getRemoteAddress() {
		return (InetSocketAddress) this.channelFuture.channel().remoteAddress();
	}

	public InetSocketAddress getLocalAddress() {
		return (InetSocketAddress) this.channelFuture.channel().localAddress();
	}

	public boolean isActive() {
		return this.channelFuture.channel().isActive();
	}

	public NettyDirectMessageClient(String serverName, int port) throws UnknownHostException, IOException {
		this.serverName = serverName;
		this.port = port;
		this.address = new InetSocketAddress(serverName, port);
	}

	public void connect(String serverName, int port) throws IOException, InterruptedException {
		this.serverName = serverName;
		this.port = port;
		this.address = new InetSocketAddress(serverName, port);
		this.connect();
	}

	public void connect() throws IOException, InterruptedException {
		workerGroup = new NioEventLoopGroup();

		try {
			bootstrap = new Bootstrap();
			bootstrap.group(workerGroup);
			bootstrap.channel(NioSocketChannel.class);
			bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
			bootstrap.handler(new ChannelInitializer<SocketChannel>() {
				@Override
				public void initChannel(SocketChannel ch) throws Exception {
					ch.pipeline().addLast(new LineBasedFrameDecoder(Integer.MAX_VALUE));
					ch.pipeline().addLast(new StringDecoder());
					ch.pipeline().addLast(new StringEncoder());
					ch.pipeline().addLast(new NettyClientHandler());
				}
			});

			channelFuture = bootstrap.connect(address).sync();

//			 send message
//			channelFuture.channel().writeAndFlush(System.lineSeparator());

			// Wait until the connection is closed.
//			channelFuture.channel().closeFuture().sync();
		} catch (Exception e) {
			e.printStackTrace();
		}
//		finally {
//			workerGroup.shutdownGracefully();
//		}
	}

	public void reconnect(String serverName, int port) throws IOException {

	}

	public void reconnect() throws IOException {

	}

	public void disconnect() throws IOException, InterruptedException {
		channelFuture.channel().closeFuture();
	}

	public void sendMessage(String message) throws IOException {
		channelFuture.channel().writeAndFlush(message + System.lineSeparator());
	}

	@Override
	public void shutdown() throws IOException, InterruptedException {
		this.disconnect();
		workerGroup.shutdownGracefully();
	}

	public class NettyClientHandler extends ChannelInboundHandlerAdapter {

		@Override
		public void channelActive(ChannelHandlerContext ctx) throws Exception {
//			ctx.writeAndFlush(UUID.randomUUID().toString());
		}

		@Override
		public void channelRead(ChannelHandlerContext ctx, Object msg) {
//			ByteBuf in = (ByteBuf) msg;
//			try {
//				while (in.isReadable()) {
//					System.out.print((char) in.readByte());
//					System.out.flush();
//				}
//			} finally {
//				ReferenceCountUtil.release(msg);
//			}
		}

		@Override
		public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
			cause.printStackTrace();
			ctx.close();
		}
	}
}
