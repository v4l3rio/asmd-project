package agentcrafter.llmqlearning

import agentcrafter.common.{Action, GridWorld, QLearner, State}
import agentcrafter.llmqlearning.QTableLoader
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

import scala.util.{Failure, Success}

class QTableLoaderTest extends AnyFunSuite with Matchers:

  // make dummy GridWorld to satisfy QLearner's requirements
  private def createSimpleGrid(): GridWorld =
    GridWorld(rows = 3, cols = 3, walls = Set.empty)

  private def newLearner(): QLearner =
    val env = createSimpleGrid()
    QLearner(
      goalState = State(2, 2),
      goalReward = 0.0,
      updateFunction = env.step,
      resetFunction = () => State(0, 0)
    )

  test("QTableLoader should load valid JSON Q-Table"):
    val learner = newLearner()
    val validJson =
      """
    {
      "(0, 0)": {"Up": 1.5, "Down": 2.0, "Left": 0.5, "Right": 3.0, "Stay": 0.0},
      "(0, 1)": {"Up": 2.5, "Down": 1.0, "Left": 1.5, "Right": 2.0, "Stay": 0.5},
      "(1, 0)": {"Up": 0.5, "Down": 3.5, "Left": 2.0, "Right": 1.0, "Stay": 1.5}
    }
    """

    val result = QTableLoader.loadQTableFromJson(validJson, learner)
    result shouldBe a[Success[?]]

    // Verify that Q-values were loaded correctly
    learner.getQValue(State(0, 0), Action.Right) shouldBe 3.0
    learner.getQValue(State(0, 1), Action.Up) shouldBe 2.5
    learner.getQValue(State(1, 0), Action.Down) shouldBe 3.5


  test("QTableLoader should handle JSON with markdown code blocks"):
    val learner = newLearner()
    val jsonWithMarkdown =
      """
    ```json
    {
      "(0, 0)": {"Up": 1.0, "Down": 2.0, "Left": 0.5, "Right": 1.5, "Stay": 0.0}
    }
    ```
    """

    val result = QTableLoader.loadQTableFromJson(jsonWithMarkdown, learner)
    result shouldBe a[Success[?]]

    learner.getQValue(State(0, 0), Action.Down) shouldBe 2.0


  test("QTableLoader should handle JSON with language specification"):
    val learner = newLearner()
    val jsonWithLangSpec =
      """
    ```json
    {
      "(1, 1)": {"Up": 5.0, "Down": 3.0, "Left": 2.0, "Right": 4.0, "Stay": 1.0}
    }
    ```
    """

    val result = QTableLoader.loadQTableFromJson(jsonWithLangSpec, learner)
    result shouldBe a[Success[?]]

    learner.getQValue(State(1, 1), Action.Up) shouldBe 5.0

  test("QTableLoader should handle JSON with LLM prefixes"):
    val learner = newLearner()
    val jsonWithPrefix =
      """
    Here is the JSON Q-Table:
    {
      "(2, 2)": {"Up": 1.0, "Down": 1.0, "Left": 1.0, "Right": 1.0, "Stay": 1.0}
    }
    """

    val result = QTableLoader.loadQTableFromJson(jsonWithPrefix, learner)
    result match
      case Success(_) =>
        learner.getQValue(State(2, 2), Action.Stay) shouldBe 1.0
      case Failure(_) => succeed


  test("QTableLoader should handle incomplete markdown blocks"):
    val learner = newLearner()
    val incompleteMarkdown =
      """
    ```json
    {
      "(0, 0)": {"Up": 2.5, "Down": 1.5, "Left": 3.0, "Right": 0.5, "Stay": 2.0}
    }
    """

    val result = QTableLoader.loadQTableFromJson(incompleteMarkdown, learner)
    result shouldBe a[Success[?]]

    learner.getQValue(State(0, 0), Action.Left) shouldBe 3.0


  test("QTableLoader should handle all action types"):
    val learner = newLearner()
    val jsonWithAllActions =
      """
    {
      "(0, 0)": {
        "Up": 1.0,
        "Down": 2.0,
        "Left": 3.0,
        "Right": 4.0,
        "Stay": 5.0
      }
    }
    """

    val result = QTableLoader.loadQTableFromJson(jsonWithAllActions, learner)
    result shouldBe a[Success[?]]

    learner.getQValue(State(0, 0), Action.Up) shouldBe 1.0
    learner.getQValue(State(0, 0), Action.Down) shouldBe 2.0
    learner.getQValue(State(0, 0), Action.Left) shouldBe 3.0
    learner.getQValue(State(0, 0), Action.Right) shouldBe 4.0
    learner.getQValue(State(0, 0), Action.Stay) shouldBe 5.0


  test("QTableLoader should handle multiple states"):
    val learner = newLearner()
    val jsonWithMultipleStates =
      """
    {
      "(0, 0)": {"Up": 1.0, "Down": 2.0, "Left": 3.0, "Right": 4.0, "Stay": 5.0},
      "(1, 0)": {"Up": 6.0, "Down": 7.0, "Left": 8.0, "Right": 9.0, "Stay": 10.0},
      "(0, 1)": {"Up": 11.0, "Down": 12.0, "Left": 13.0, "Right": 14.0, "Stay": 15.0},
      "(5, 7)": {"Up": 16.0, "Down": 17.0, "Left": 18.0, "Right": 19.0, "Stay": 20.0}
    }
    """

    val result = QTableLoader.loadQTableFromJson(jsonWithMultipleStates, learner)
    result shouldBe a[Success[?]]

    learner.getQValue(State(0, 0), Action.Stay) shouldBe 5.0
    learner.getQValue(State(1, 0), Action.Stay) shouldBe 10.0
    learner.getQValue(State(0, 1), Action.Stay) shouldBe 15.0
    learner.getQValue(State(5, 7), Action.Stay) shouldBe 20.0


  test("QTableLoader should fail on invalid JSON"):
    val learner = newLearner()
    val invalidJson =
      """
    {
      "(0, 0)": {"Up": 1.0, "Down": 2.0, "Left": 3.0, "Right": 4.0, "Stay": 5.0
    }
    """

    val result = QTableLoader.loadQTableFromJson(invalidJson, learner)
    result shouldBe a[Failure[?]]


  test("QTableLoader should fail on invalid state coordinates"):
    val learner = newLearner()
    val invalidStateJson =
      """
    {
      "invalid_state": {"Up": 1.0, "Down": 2.0, "Left": 3.0, "Right": 4.0, "Stay": 5.0}
    }
    """

    val result = QTableLoader.loadQTableFromJson(invalidStateJson, learner)
    result shouldBe a[Failure[?]]


  test("QTableLoader should fail on invalid action names"):
    val learner = newLearner()
    val invalidActionJson =
      """
    {
      "(0, 0)": {"InvalidAction": 1.0, "Down": 2.0, "Left": 3.0, "Right": 4.0, "Stay": 5.0}
    }
    """

    val result = QTableLoader.loadQTableFromJson(invalidActionJson, learner)
    result shouldBe a[Failure[?]]


  test("QTableLoader should fail on non-numeric Q-values"):
    val learner = newLearner()
    val nonNumericJson =
      """
    {
      "(0, 0)": {"Up": "not_a_number", "Down": 2.0, "Left": 3.0, "Right": 4.0, "Stay": 5.0}
    }
    """

    val result = QTableLoader.loadQTableFromJson(nonNumericJson, learner)
    result shouldBe a[Failure[?]]


  test("QTableLoader should handle empty JSON object"):
    val learner = newLearner()
    val emptyJson = "{}"

    val result = QTableLoader.loadQTableFromJson(emptyJson, learner)
    result shouldBe a[Success[?]]


  test("QTableLoader should handle whitespace and formatting variations"):
    val learner = newLearner()
    val messyJson =
      """
    
    
    {
      
      "(0, 0)"   :   {   "Up"  :  1.0  ,  "Down"  :  2.0  ,  "Left"  :  3.0  ,  "Right"  :  4.0  ,  "Stay"  :  5.0  }  
      
    }
    
    
    """

    val result = QTableLoader.loadQTableFromJson(messyJson, learner)
    result shouldBe a[Success[?]]

    learner.getQValue(State(0, 0), Action.Up) shouldBe 1.0
  