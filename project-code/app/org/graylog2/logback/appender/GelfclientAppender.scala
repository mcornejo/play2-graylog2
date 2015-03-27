package org.graylog2.logback.appender

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.AppenderBase
import org.graylog2.gelfclient.GelfMessage
import org.graylog2.gelfclient.GelfMessageBuilder
import org.graylog2.gelfclient.GelfMessageLevel
import org.graylog2.gelfclient.transport.GelfTransport

case class GelfclientAppender(private val transport: GelfTransport, private val hostname: String) extends AppenderBase[ILoggingEvent] {
  override protected def append(eventObject: ILoggingEvent) {
    append(convertToGelfMessage(eventObject))
  }

  def append(gelfMessage: GelfMessage): Unit = {
    try {
      transport.send(gelfMessage)
    }
    catch {
      case e: InterruptedException => e.printStackTrace()
    }
  }

  private def convertToGelfMessage(event: ILoggingEvent): GelfMessage = {
    new GelfMessageBuilder(event.getFormattedMessage, hostname).timestamp(event.getTimeStamp / 1000d).level(toGelfMessageLevel(event.getLevel)).additionalField("threadname", event.getThreadName).additionalField("logger", event.getLoggerName).build
  }

  private def toGelfMessageLevel(level: Level): GelfMessageLevel = {
    level.toInt match {
      case Level.ERROR_INT =>
        GelfMessageLevel.ERROR
      case Level.WARN_INT =>
        GelfMessageLevel.WARNING
      case Level.DEBUG_INT =>
        GelfMessageLevel.DEBUG
      case _ =>
        GelfMessageLevel.INFO
    }
  }
}
