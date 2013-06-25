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

case class ItemRelocated(f: BoxFile)
case class ItemRenamed(f: BoxFile)
case class CommentAddedTo(comment: BoxComment, f: BoxFileSystemElement)
case class CommentModified(comment: BoxComment, f: BoxFileSystemElement)
case class CommentTrashed(comment: BoxComment, f: BoxFileSystemElement)
case class NewFile(f: BoxFile)
case class UpdatedFile(f: BoxFile)
case class ProjectSetFor(project: ProjectLike, f: BoxFileSystemElement)
case class ProjectSetForComment(project: ProjectLike, comment: BoxComment)

class BoxFileSystemTreeActor(root: BoxFolder) extends Actor with PlayActorSystem {
  val tree = Agent[BoxFileSystemTree](BoxFileSystemTree.createFrom(root))
  
  implicit val treeActor = context.self
  
  def receive = {
    case Add(f) => tree.send(_.insert(f.path, BoxFileSystemTree.createFrom(f)))
    
    case AddCollaborations(f, collaborations) => tree.send(_.addCollaborators(f.fullPath, collaborations))
    
    case Trash(f) => tree.send(_.trash(f.fullPath))
      
    case AddComment(f, comment) => tree.send(_.addComment(f.fullPath, comment))
  }
  
  
}

object BoxFileSystemTreeActor {
  def props(root: BoxFolder, name: String): Props = Props(() => new BoxFileSystemTreeActor(root), name)
}