package models

case class Workspace(name: String, phases: List[DPhase])

object workspace extends BasicDAO[Workspace]("workspaces"){
  
}