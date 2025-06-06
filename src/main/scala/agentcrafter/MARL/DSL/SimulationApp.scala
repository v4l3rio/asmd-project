package agentcrafter.MARL.DSL

object SimulationApp extends App with SimulationDSL:
  import AgentProperty.*
  import TriggerProperty.*
  import LearnerProperty.*
  import WallProperty.*
  import LineProperty.*
  import SimulationProperty.*
  
  simulation:
    grid:
      10 x 10
    walls:
      line:
        Direction >> "vertical"
        From >> (1, 3)
        To >> (1, 5)
      line:
        Direction >> "vertical"
        From >> (1, 3)
        To >> (3, 3)
      line:
        Direction >> "vertical"
        From >> (1, 5)
        To >> (3, 5)
      line:
        Direction >> "vertical"
        From >> (3, 3)
        To >> (3, 5)
      block >> (7, 7)
    agent:
      Name >> "Runner"
      Start >> (1, 9)
      withLearner:
        Alpha >> 0.1
        Gamma >> 0.99
        Eps0 >> 0.9
        EpsMin >> 0.05
        Warm >> 1_000
        Optimistic >> 0.5
      Goal >> (2, 4)
      onGoal:
        Give >> 100
        EndEpisode >> true
    Episodes >> 10_000
    Steps >> 400
    ShowAfter >> 9_000
    Delay >> 100
    WithGUI >> true