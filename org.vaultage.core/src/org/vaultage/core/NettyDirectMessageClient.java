package org.vaultage.core;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.util.UUID;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.ReferenceCountUtil;

public class NettyDirectMessageClient implements DirectMessageClient {

	String serverName;
	int port;

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

	public NettyDirectMessageClient(String serverName, int port) throws UnknownHostException, IOException {
		this.serverName = serverName;
		this.port = port;
	}

	public void connect(String serverName, int port) throws IOException {
		this.serverName = serverName;
		this.port = port;
	}

	public void connect() throws IOException, InterruptedException {
		EventLoopGroup workerGroup = new NioEventLoopGroup();

		try {
			Bootstrap b = new Bootstrap(); // (1)
			b.group(workerGroup); // (2)
			b.channel(NioSocketChannel.class); // (3)
			b.option(ChannelOption.SO_KEEPALIVE, true); // (4)
			b.handler(new ChannelInitializer<SocketChannel>() {
				@Override
				public void initChannel(SocketChannel ch) throws Exception {
					
					ch.pipeline().addLast(new StringDecoder());
					ch.pipeline().addLast(new StringEncoder());
					ch.pipeline().addLast(new NettyClientHandler());
				}
			});

			// Start the client.
			ChannelFuture f = b.connect(serverName, port).sync(); // (5)

			// send message
			f.channel().write(UUID.randomUUID().toString() + System.lineSeparator());
			f.channel().flush();

			// Wait until the connection is closed.
			f.channel().closeFuture().sync();

		} finally {
			workerGroup.shutdownGracefully();
		}
	}

	public void reconnect(String serverName, int port) throws IOException {

	}

	public void reconnect() throws IOException {

	}

	public void disconnect() throws IOException {

	}

	public void sendMessage(String message) throws IOException {

	}

	@Override
	public void shutdown() throws IOException, InterruptedException {

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
