package com.github.alikemalocalan.greentunnel4jvm

import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.ChannelFuture
import io.netty.channel.ChannelInitializer
import io.netty.channel.ChannelOption
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.util.internal.logging.InternalLoggerFactory


object HttpProxyServer {
    val logger = InternalLoggerFactory.getInstance(this::class.java)

    val probs = System.getProperties()

    @JvmStatic
    fun newProxyService(port: Int = 8080, threadCount: Int = 25): ChannelFuture {
        logger.debug("HttpProxyServer started on :${port}")
        val workerGroup = NioEventLoopGroup(threadCount)

        val bootstrap: ServerBootstrap = ServerBootstrap()
            .group(workerGroup)
            .channel(NioServerSocketChannel::class.java)
            .option(ChannelOption.SO_BACKLOG, 1024)
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 1000)


        return bootstrap.childHandler(object : ChannelInitializer<SocketChannel>() {
            override fun initChannel(ch: SocketChannel) {
                ch.pipeline().addLast(
                    HttpProxyClientHandler()
                )
            }
        })
            .bind(port)
            .sync()
            .channel()
            .closeFuture()
            .sync()
    }


    @JvmStatic
    fun main(args: Array<String>) {
        val port = probs["proxy.port"]

        if (port != null) {
            logger.warn("Server Port :$port")
            newProxyService(port = port.toString().toInt())
        } else newProxyService()
    }


    fun stop(server: ChannelFuture): Boolean = server.cancel(false)

}