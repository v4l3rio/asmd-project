package agentcrafter.llmqlearning

import scala.annotation.targetName

/**
 * DSL properties for LLM configuration
 */
enum LLMProperty[T]:
  case Enabled extends LLMProperty[Boolean]
  case Model extends LLMProperty[String]

  @targetName("setProperty")
  infix def >>(value: T)(using config: LLMConfig): Unit = this match
    case LLMProperty.Enabled => config.enabled = value.asInstanceOf[Boolean]
    case LLMProperty.Model => config.model = value.asInstanceOf[String]
