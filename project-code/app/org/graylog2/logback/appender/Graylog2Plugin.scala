package org.graylog2.logback.appender

import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.LoggerContext
import com.google.common.net.HostAndPort
import org.graylog2.gelfclient.GelfConfiguration
import org.graylog2.gelfclient.GelfTransports
import org.graylog2.gelfclient.transport.GelfTransport
import org.slf4j.LoggerFactory
import play.Application
import play.Configuration
import play.Plugin
import java.net.InetAddress
import java.net.UnknownHostException

class Graylog2Plugin(app: Application) extends Plugin {
  private val log: org.slf4j.Logger = LoggerFactory.getLogger(classOf[Graylog2Plugin])
  private val config: Configuration = app.configuration()

  private val accessLogEnabled: Boolean = config.getBoolean("graylog2.appender.send-access-log", false)
  private val queueCapacity: Int = config.getInt("graylog2.appender.queue-size", 512)
  private val reconnectInterval: Long = config.getMilliseconds("graylog2.appender.reconnect-interval", 500L)
  private val connectTimeout: Long = config.getMilliseconds("graylog2.appender.connect-timeout", 1000L)
  private val isTcpNoDelay: Boolean = config.getBoolean("graylog2.appender.tcp-nodelay", false)
  private val sendBufferSize: Int = config.getInt("graylog2.appender.sendbuffersize", 0) // causes the socket default to be used

  val canonicalHostName: String = try {
    config.getString("graylog2.appender.sourcehost", InetAddress.getLocalHost.getCanonicalHostName)
  }
  catch {
    case e: UnknownHostException => {
      log.error("Unable to resolve canonical localhost name. " + "Please set it manually via graylog2.appender.sourcehost or fix your lookup service, falling back to {}", "localhost")
      "localhost"
    }
  }
  private val hostString: String = config.getString("graylog2.appender.host", "127.0.0.1:12201")
  private val protocol: String = config.getString("graylog2.appender.protocol", "tcp")
  private val hostAndPort: HostAndPort = HostAndPort.fromString(hostString)
  private val gelfTransport: GelfTransports = GelfTransports.valueOf(protocol.toUpperCase)
  private val gelfConfiguration: GelfConfiguration = new GelfConfiguration(hostAndPort.getHostText, hostAndPort.getPort)
    .transport(gelfTransport)
    .reconnectDelay(reconnectInterval.intValue)
    .queueSize(queueCapacity)
    .connectTimeout(connectTimeout.intValue)
    .tcpNoDelay(isTcpNoDelay)
    .sendBufferSize(sendBufferSize)

  private val transport: GelfTransport = GelfTransports.create(gelfConfiguration)
  private val lc: LoggerContext = LoggerFactory.getILoggerFactory.asInstanceOf[LoggerContext]
  private val rootLogger: Logger = lc.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME)
  val gelfAppender: GelfclientAppender = new GelfclientAppender(transport, canonicalHostName)
  gelfAppender.setContext(lc)

  override def onStart() = {
    gelfAppender.start()
    rootLogger.addAppender(gelfAppender)
  }

  override def onStop() = {
    rootLogger.detachAppender(gelfAppender)
    transport.stop()
  }

  def getLocalHostName: String = {
    canonicalHostName
  }

  def getGelfAppender: GelfclientAppender = {
    gelfAppender
  }

  def isAccessLogEnabled: Boolean = {
    accessLogEnabled
  }

}