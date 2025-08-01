package agentcrafter.llmqlearning.dsl
import scala.annotation.targetName

/**
 * Configuration parameters for LLM-generated wall creation.
 *
 * This class provides mutable configuration parameters for generating walls using Large Language Models through the DSL
 * syntax.
 *
 * @param model
 *   The LLM model identifier to use for wall generation
 * @param prompt
 *   The prompt template for requesting wall layouts from the LLM
 */
case class LLMWallConfig(
  var model: String = "",
  var prompt: String = ""
)

/**
 * DSL properties for configuring LLM-generated wall creation.
 *
 * This enumeration provides type-safe property setters for LLM wall generation configuration through the DSL syntax.
 * Each case corresponds to a specific LLM configuration parameter.
 *
 * @tparam T
 *   The type of value this property accepts
 */
enum LLMWallProperty[T]:
  /** The LLM model identifier to use for wall generation */
  case Model extends LLMWallProperty[String]

  /** The prompt template for requesting wall layouts from the LLM */
  case Prompt extends LLMWallProperty[String]

  @targetName("to")
  infix def >>(obj: T)(using config: LLMWallConfig): Unit = this match
    case LLMWallProperty.Model => config.model = obj.asInstanceOf[String]
    case LLMWallProperty.Prompt => config.prompt = obj.asInstanceOf[String]
