package projectZoom.connector.box

import akka.actor._
import akka.agent._
import projectZoom.util.PlayActorSystem
import models.ProjectLike

import api._

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
case class NewFile(f: BoxFile)
case class UpdatedFile(f: BoxFile)
case class ProjectSetFor(project: ProjectLike, f: BoxFileSystemElement)
case class ProjectSetForComment(project: ProjectLike, comment: BoxComment)
case class CollaboratorsChangedFor(collaborators: Set[String], f: BoxFileSystemElement)

class BoxFileSystemTreeActor(root: BoxFolder, fileProjectMatcher: ActorRef) extends Actor with PlayActorSystem {
  val tree = Agent[BoxFileSystemTree](BoxFileSystemTree.createFrom(root))
  val ids = Agent[Map[String, List[String]]](Map())
  
  implicit val treeActor = context.self
  val parent = context.parent
  
  def receive = {
    case Add(f) => tree.send(_.insert(f.path, BoxFileSystemTree.createFrom(f)))
    
    case AddCollaborations(f, collaborations) => tree.send(_.addCollaborators(f.fullPath, collaborations))
    
    case Trash(f) => tree.send(_.trash(f.fullPath))
      
    case AddComment(f, comment) => tree.send(_.addComment(f.fullPath, comment))

    case Rename(f) => tree.send(_.rename(ids()(f.id), f.name))
    
    case NewFile(f: BoxFile) => 
      ids.send(_ + (f.id -> f.fullPath))
      parent ! NewFile(f)
    
    case ItemRelocated(f) => 
      ids.send(_ + (f.id -> f.fullPath))
      parent ! ItemRelocated(f)
    
    case ItemRenamed(f) => 
      ids.send(_ + (f.id -> f.fullPath))
      parent ! ItemRenamed(f)
      
    case CollaboratorsChangedFor(coll, f) =>
      fileProjectMatcher ! MatchFile(coll, )
    
    
  }
  
  
}

object BoxFileSystemTreeActor {
  def props(root: BoxFolder, fileProjectMatcher: ActorRef, name: String): Props = Props(() => new BoxFileSystemTreeActor(root, fileProjectMatcher), name)
}