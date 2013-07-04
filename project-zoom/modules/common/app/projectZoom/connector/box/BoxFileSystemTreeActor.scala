package projectZoom.connector.box

import akka.actor._
import akka.agent._
import projectZoom.util.PlayActorSystem
import models.ProjectLike
import play.Logger

import api._

case class InitializeTree(root: BoxFolder)
case object ResetTree
case object PrintTree

case class Add(f: BoxFileSystemElement)
case class AddCollaborations(f: BoxFolder, collaborations: Set[String])
case class Trash(f: BoxFileSystemElement)
case class AddComment(f: BoxFileSystemElement, comment: BoxComment)
case class Rename(f: BoxFileSystemElement)

case class ItemRelocated(f: BoxFile)
case class ItemRenamed(f: BoxFile)
case class CommentAddedTo(comment: BoxComment, f: BoxFileSystemElement)
case class CommentModified(comment: BoxComment, f: BoxFileSystemElement)
case class CommentTrashed(comment: BoxComment, f: BoxFileSystemElement)
case class CommentUnderlyingRenamed(comment: BoxComment, f: BoxFileSystemElement)
case class CommentUnderlyingRelocated(comment: BoxComment, f: BoxFileSystemElement)
case class NewFile(f: BoxFile, collaborators: Set[String])
case class UpdatedFile(f: BoxFile, collaborators: Set[String])
case class ProjectSetFor(project: ProjectLike, f: BoxFileSystemElement)
case class ProjectSetForComment(project: ProjectLike, comment: BoxComment)
case class CollaboratorsChangedFor(collaborators: Set[String], f: BoxFileSystemElement)

class BoxFileSystemTreeActor(fileProjectMatcher: ActorRef) extends Actor with PlayActorSystem {
  import context.become
  
  val tree = Agent[Option[BoxFileSystemTree]](None)
  val ids = Agent[Map[String, List[String]]](Map())
  
  implicit val treeActor = context.self
  val parent = context.parent
  
  def alterTree(f: BoxFileSystemTree => BoxFileSystemTree) = tree.send(_.map(f(_)))
  
  def printTree = tree().map(someTree => Logger.info(someTree.prettyPrinted(0)))

  def uninitialized: Receive = {
    case InitializeTree(root) => 
      tree.send(Some(BoxFileSystemTree.createFrom(root)))
      Logger.info("Initializing BoxTree")
      become(initialized)
  }
  
  def initialized: Receive = {
    case ResetTree =>
      Logger.info("Resetting BoxTree")
      tree send None
      ids send Map[String,List[String]]()
      become(uninitialized)
    
    case Add(f) => 
      alterTree(_.insert(f.path, BoxFileSystemTree.createFrom(f)))
    
    case AddCollaborations(f, collaborations) => alterTree(_.addCollaborators(f.fullPath, collaborations))
    
    case Trash(f) => alterTree(t => t.trash(f.fullPath))
      
    case AddComment(f, comment) => alterTree(_.addComment(f.fullPath, comment))

    case Rename(f) => alterTree(_.rename(ids()(f.id), f.name))
    
    case NewFile(f: BoxFile, collaborators: Set[String]) => 
      ids.send(_ + (f.id -> f.fullPath))
      parent ! NewFile(f, collaborators)
    
    case ItemRelocated(f) => 
      ids.send(_ + (f.id -> f.fullPath))
      parent ! ItemRelocated(f)
    
    case ItemRenamed(f) => 
      ids.send(_ + (f.id -> f.fullPath))
      parent ! ItemRenamed(f)
    
    case PrintTree => 
      printTree
  }
  
  def receive = uninitialized
}

object BoxFileSystemTreeActor {
  def props(fileProjectMatcher: ActorRef, name: String): Props = Props(() => new BoxFileSystemTreeActor(fileProjectMatcher), name)
}