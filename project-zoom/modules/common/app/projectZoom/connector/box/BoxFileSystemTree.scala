package projectZoom.connector.box

import api._
import akka.actor._
import models.ProjectLike

sealed trait BoxFileSystemTree {
  val reporter: ActorRef
  val element: BoxFileSystemElement
  val collaborators: Set[String]
  val comments: List[BoxComment]
  val trashed: Boolean
  val project: Option[ProjectLike]

  def findAllFiles: List[TreeFile]
  def insert(atPath: List[String], tree: BoxFileSystemTree): BoxFileSystemTree
  def remove(atPath: List[String]): BoxFileSystemTree
  def addCollaborators(atPath: List[String], newCollaborators: Set[String]): BoxFileSystemTree
  def trash(atPath: List[String]): BoxFileSystemTree
  def addComment(atPath: List[String], comment: BoxComment): BoxFileSystemTree
  def rename(atPath: List[String], name: String): BoxFileSystemTree
  def relocate(index: Int, name: String): BoxFileSystemTree
  def setProject(atPath: List[String], project: Option[ProjectLike]): BoxFileSystemTree
}

case class TreeFile(element: BoxFile, reporter: ActorRef, collaborators: Set[String], comments: List[BoxComment], project: Option[ProjectLike], trashed: Boolean) extends BoxFileSystemTree {
  def findAllFiles: List[TreeFile] = List(this)
  def insert(atPath: List[String], tree: BoxFileSystemTree) = throw new UnsupportedOperationException
  def remove(atPath: List[String]) = throw new UnsupportedOperationException

  def setProject(atPath: List[String], project: Option[ProjectLike]) = 
    if(atPath.isEmpty){
      project.foreach{p => 
      reporter ! ProjectSetFor(p, element)
      comments.foreach(c => reporter ! ProjectSetForComment(p, c))        
      }
      this.copy(project = project)
    } else throw new NoSuchElementException
  
  def addCollaborators(atPath: List[String], newCollaborators: Set[String]) =
    if (atPath.isEmpty) this.copy(collaborators = collaborators union newCollaborators)
    else throw new NoSuchElementException

  def trash(atPath: List[String]) =
    if (atPath.isEmpty) {
      reporter ! Trash(element)
      this.copy(trashed = true)
    }
    else throw new NoSuchElementException

  def addComment(atPath: List[String], comment: BoxComment) =
    if (atPath.isEmpty) {
      val i = comments.indexWhere(_.id == comment.id)
      if (i == -1) {
        val result = this.copy(comments = comments :+ comment)
        reporter ! CommentAddedTo(comment, element)
        result
      } else {
        val result = this.copy(comments = comments.updated(i, comment))
        reporter ! CommentModified(comment, element)
        result
      }
    } else throw new NoSuchElementException

  def rename(atPath: List[String], name: String) = {
    if (atPath.isEmpty) {
      val result = this.copy(element = element.rename(name))
      reporter ! ItemRenamed(result.element)
      result
    } else throw new NoSuchElementException
  }

  def relocate(index: Int, name: String) = {
    val result = this.copy(element = element.relocate(index, name))
    reporter ! ItemRelocated(result.element)
    result
  }
}

case class TreeFolder(element: BoxFolder, reporter: ActorRef, collaborators: Set[String], comments: List[BoxComment], project: Option[ProjectLike], trashed: Boolean, children: Map[String, BoxFileSystemTree]) extends BoxFileSystemTree {
  def findAllFiles: List[TreeFile] = children.flatMap(t => t._2.findAllFiles).toList

  def insert(atPath: List[String], tree: BoxFileSystemTree) = {
    if (atPath.isEmpty) {
      val result = this.copy(children = children + (tree.element.name -> tree.addCollaborators(Nil, collaborators).setProject(Nil, project)))
      if(children.contains(tree.element.name))
        tree match{
          case f: TreeFile => reporter ! UpdatedFile(f.element)
          case _ => 
      }
      else {
        tree match {
          case f: TreeFile => reporter ! NewFile(f.element)
          case _ => 
        }
      }
      result
    }
    else this.copy(children = children + (atPath.head -> children(atPath.head).insert(atPath.tail, tree)))
  }

  def remove(atPath: List[String]) = {
    if (atPath.isEmpty && element.id == "0") throw new UnsupportedOperationException("The root folder may not be deleted")
    if (atPath.size == 1) this.copy(children = children - atPath.head)
    else this.copy(children = children + (atPath.head -> children(atPath.head).remove(atPath.tail)))
  }

  def addCollaborators(atPath: List[String], newCollaborators: Set[String]) = {
    if (atPath.isEmpty) this.copy(collaborators = collaborators.union(newCollaborators), children = children.mapValues(c => c.addCollaborators(Nil, newCollaborators)))
    else this.copy(children = children + (atPath.head -> children(atPath.head).addCollaborators(atPath.tail, newCollaborators)))
  }

  def trash(atPath: List[String]) = {
    if (atPath.isEmpty) {
      val result = this.copy(trashed = true, children = children.mapValues(c => c.trash(Nil)))
      comments.foreach { comment => reporter ! CommentTrashed(comment, element) }
      result
    } else this.copy(children = children + (atPath.head -> children(atPath.head).trash(atPath.tail)))
  }

  def addComment(atPath: List[String], comment: BoxComment) = {
    if (atPath.isEmpty) {
      val i = comments.indexWhere(_.id == comment.id)
      if (i == -1) {
        val result = this.copy(comments = comments :+ comment)
        reporter ! CommentAddedTo(comment, element)
        result
      } else {
        val result = this.copy(comments = comments.updated(i, comment))
        reporter ! CommentModified(comment, element)
        result
      }

    } else this.copy(children = children + (atPath.head -> children(atPath.head).addComment(atPath.tail, comment)))
  }

  def rename(atPath: List[String], name: String) = {
    if (atPath.isEmpty) {
      this.copy(element = element.rename(name), children = children.mapValues(c => c.relocate(element.path.size, name)))
    } else if (atPath.size == 1) this.copy(children = children - atPath.head + (name -> children(atPath.head).rename(Nil, name)))
    else this.copy(children = children + (atPath.head -> children(atPath.head).rename(atPath.tail, name)))
  }

  def relocate(index: Int, name: String) = {
    this.copy(element = element.relocate(index, name), children = children.mapValues(c => c.relocate(index, name)))
  }

  def setProject(atPath: List[String], project: Option[ProjectLike]) = {
    if (atPath.isEmpty) {
      val result = this.copy(project = project, children = children.mapValues(c => c.setProject(Nil, project)))
      project.foreach{p => 
      reporter ! ProjectSetFor(p, element)
      comments.foreach(c => reporter ! ProjectSetForComment(p, c)) 
      }
      result
    } else this.copy(children = children + (atPath.head -> children(atPath.head).setProject(atPath.tail, project)))
  }
}

object BoxFileSystemTree {
  def createFrom(f: BoxFileSystemElement)(implicit reporter: ActorRef) = f match {
    case f: BoxFile => TreeFile(f, reporter, Set(), Nil, None, false)
    case f: BoxFolder => TreeFolder(f, reporter, Set(), Nil, None, false, Map())
  }
}

