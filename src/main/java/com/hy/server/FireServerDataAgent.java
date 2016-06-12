package com.hy.server;

import com.hy.decoder.NettyMessageDecoder;
import com.hy.encoder.NettyMessageEncoder;
import com.hy.handler.FireServerDataHandler;
import com.hy.handler.FireServerHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.apache.log4j.Logger;

/**
 * Created by cpazstido on 2016/6/12.
 */
public class FireServerDataAgent extends ServerAgent {
    private static Logger logger = Logger.getLogger(FireServerDataAgent.class);

    public FireServerDataAgent(String serverName, int port) {
        this.setServerName(serverName);
        this.setPort(port);
    }

    public void bind() {
        // 配置服务端的NIO线程组
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 100)
                    .option(ChannelOption.RCVBUF_ALLOCATOR, new FixedRecvByteBufAllocator(2048))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        public void initChannel(SocketChannel ch)
                                throws Exception {
                            ch.pipeline().addLast(
                                    new FireServerDataHandler());
                        }
                    });
            // 绑定端口，同步等待成功
            ChannelFuture f = b.bind(getPort()).sync();
            logger.debug(getServerName() + " starts successfully！begin to listen at:" + getPort());

            // 等待服务端监听端口关闭
            f.channel().closeFuture().sync();
        } catch (Exception e) {
            logger.error(e);
        } finally {
            // 优雅退出，释放线程池资源
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
