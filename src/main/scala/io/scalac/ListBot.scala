package io.scalac

import io.scalac.slack.MessageEventBus
import io.scalac.slack.bots.AbstractBot
import io.scalac.slack.common.{BaseMessage, Command, OutboundMessage}

class ListBot(override val bus: MessageEventBus) extends AbstractBot {

  var set: Set[String] = Set()

  override def help(channel: String): OutboundMessage =
    OutboundMessage(channel,
      s"$name will help you to solve difficult math problems \\n" +
        "Usage: $calc {operation} {arguments separated by space}")

  val operations = Map(
    "addme" -> ((message: BaseMessage, user: Option[String]) => set + user.getOrElse(message.user)),
    "add" -> ((message: BaseMessage, user: Option[String]) => set + user.getOrElse(message.user)),
    "removeme" -> ((message: BaseMessage, user: Option[String]) => set - user.getOrElse(message.user)),
    "remove" -> ((message: BaseMessage, user: Option[String]) => set - user.getOrElse(message.user)),
    "clear" -> ((message: BaseMessage, user: Option[String]) => Set[String]())
  )


  override def act: Receive = {
    case Command("listBuilder", operation :: args, message) if args.nonEmpty =>
      val op = operations.get(operation)

      val response = op.map(f => {
        set = f(message, args.headOption)
        OutboundMessage(message.channel, s"The list is: ${set.mkString("\n")}")
      }).getOrElse(OutboundMessage(message.channel, s"No operation $operation"))

      publish(response)

    case Command("listBuilder", _, message) =>
      set = set + message.user
      publish(OutboundMessage(message.channel, set.mkString("/n")))
  }
}