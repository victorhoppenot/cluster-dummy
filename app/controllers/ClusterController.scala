package controllers

import javax.inject._
import play.api.mvc._
import play.api.libs.json._
import model._
import scala.concurrent.{ExecutionContext, Future}
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.MySQLProfile.api._

@Singleton
class ClusterController @Inject()(val controllerComponents: ControllerComponents, dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) extends BaseController {

  // User Routes
  def createUser = Action.async(parse.json) { request =>
    val userResult = request.body.validate[UserConnection]
    userResult.fold(
      errors => Future.successful(BadRequest(Json.obj("message" -> JsError.toJson(errors)))),
      user => {
        User.create(user.username, user.hashedKey, user.biography, user.community).map {
          case Some(newUser) => Created(Json.toJson(newUser))
          case None => InternalServerError(Json.obj("message" -> "Failed to create user"))
        }
      }
    )
  }

  def getUser(id: String) = Action.async {
    User.findById(Identity[User](id)).map {
      case Some(user) => Ok(Json.toJson(user))
      case None => NotFound(Json.obj("message" -> "User not found"))
    }
  }

  // Clique Routes
  def createClique = Action.async(parse.json) { request =>
    val cliqueResult = request.body.validate[CliqueConnection]
    cliqueResult.fold(
      errors => Future.successful(BadRequest(Json.obj("message" -> JsError.toJson(errors)))),
      clique => {
        Clique.create(clique.name, clique.biography).map {
          case Some(newClique) => Created(Json.toJson(newClique))
          case None => InternalServerError(Json.obj("message" -> "Failed to create clique"))
        }
      }
    )
  }

  def getClique(id: String) = Action.async {
    Clique.findById(Identity[Clique](id)).map {
      case Some(clique) => Ok(Json.toJson(clique))
      case None => NotFound(Json.obj("message" -> "Clique not found"))
    }
  }

  // Cluster Routes
  def createCluster = Action.async(parse.json) { request =>
    val clusterResult = request.body.validate[ClusterConnection]
    clusterResult.fold(
      errors => Future.successful(BadRequest(Json.obj("message" -> JsError.toJson(errors)))),
      cluster => {
        Cluster.create(cluster.title, cluster.parentId).map {
          case Some(newCluster) => Created(Json.toJson(newCluster))
          case None => InternalServerError(Json.obj("message" -> "Failed to create cluster"))
        }
      }
    )
  }

  def getCluster(id: String) = Action.async {
    Cluster.findById(Identity[Cluster](id)).map {
      case Some(cluster) => Ok(Json.toJson(cluster))
      case None => NotFound(Json.obj("message" -> "Cluster not found"))
    }
  }

  // Community Routes
  def createCommunity = Action.async(parse.json) { request =>
    val communityResult = request.body.validate[CommunityConnection]
    communityResult.fold(
      errors => Future.successful(BadRequest(Json.obj("message" -> JsError.toJson(errors)))),
      community => {
        Community.create(community.title, community.area_geojson).map {
          case Some(newCommunity) => Created(Json.toJson(newCommunity))
          case None => InternalServerError(Json.obj("message" -> "Failed to create community"))
        }
      }
    )
  }

  def getCommunity(id: String) = Action.async {
    Community.findById(Identity[Community](id)).map {
      case Some(community) => Ok(Json.toJson(community))
      case None => NotFound(Json.obj("message" -> "Community not found"))
    }
  }

  // Post Routes
  def createPost = Action.async(parse.json) { request =>
    val postResult = request.body.validate[PostConnection]
    postResult.fold(
      errors => Future.successful(BadRequest(Json.obj("message" -> JsError.toJson(errors)))),
      post => {
        Post.create(post.caption, post.song, post.poster, post.clique, post.cluster).map {
          case Some(newPost) => Created(Json.toJson(newPost))
          case None => InternalServerError(Json.obj("message" -> "Failed to create post"))
        }
      }
    )
  }

  def getPost(id: String) = Action.async {
    Post.findById(Identity[Post](id)).map {
      case Some(post) => Ok(Json.toJson(post))
      case None => NotFound(Json.obj("message" -> "Post not found"))
    }
  }

  // Comment Routes
  def createComment = Action.async(parse.json) { request =>
    val commentResult = request.body.validate[CommentConnection]
    commentResult.fold(
      errors => Future.successful(BadRequest(Json.obj("message" -> JsError.toJson(errors)))),
      comment => {
        Comment.create(comment.content, comment.author, Some(comment.parent)).map {
          case Some(newComment) => Created(Json.toJson(newComment))
          case None => InternalServerError(Json.obj("message" -> "Failed to create comment"))
        }
      }
    )
  }

  def getComment(id: String) = Action.async {
    Comment.findById(Identity[Comment](id)).map {
      case Some(comment) => Ok(Json.toJson(comment))
      case None => NotFound(Json.obj("message" -> "Comment not found"))
    }
  }

  // Song Routes
  def createSong = Action.async(parse.json) { request =>
    val songResult = request.body.validate[SongConnection]
    songResult.fold(
      errors => Future.successful(BadRequest(Json.obj("message" -> JsError.toJson(errors)))),
      song => {
        Song.create(song.title, song.spotify_id, song.apple_music_id).map {
          case Some(newSong) => Created(Json.toJson(newSong))
          case None => InternalServerError(Json.obj("message" -> "Failed to create song"))
        }
      }
    )
  }

  def getSong(id: String) = Action.async {
    Song.findById(Identity[Song](id)).map {
      case Some(song) => Ok(Json.toJson(song))
      case None => NotFound(Json.obj("message" -> "Song not found"))
    }
  }

  // Achievement Routes
  def createUserAchievement = Action.async(parse.json) { request =>
    val achievementResult = request.body.validate[UserAchievementConnection]
    achievementResult.fold(
      errors => Future.successful(BadRequest(Json.obj("message" -> JsError.toJson(errors)))),
      achievement => {
        UserAchievement.create(achievement.name).map {
          case Some(newAchievement) => Created(Json.toJson(newAchievement))
          case None => InternalServerError(Json.obj("message" -> "Failed to create achievement"))
        }
      }
    )
  }

  def getUserAchievement(id: String) = Action.async {
    UserAchievement.findById(Identity[UserAchievement](id)).map {
      case Some(achievement) => Ok(Json.toJson(achievement))
      case None => NotFound(Json.obj("message" -> "Achievement not found"))
    }
  }

  // Association Routes
  def createUserCliqueAssociation = Action.async(parse.json) { request =>
    val associationResult = request.body.validate[UserCliqueAssociationConnection]
    associationResult.fold(
      errors => Future.successful(BadRequest(Json.obj("message" -> JsError.toJson(errors)))),
      association => {
        UserCliqueAssociation.create(association.subj, association.obj, association.memberRole).map {
          case Some(newAssociation) => Created(Json.toJson(newAssociation))
          case None => InternalServerError(Json.obj("message" -> "Failed to create association"))
        }
      }
    )
  }

  def createUserClusterAssociation = Action.async(parse.json) { request =>
    val associationResult = request.body.validate[UserClusterAssociationConnection]
    associationResult.fold(
      errors => Future.successful(BadRequest(Json.obj("message" -> JsError.toJson(errors)))),
      association => {
        UserClusterAssociation.create(association.subj, association.obj, association.memberRole).map {
          case Some(newAssociation) => Created(Json.toJson(newAssociation))
          case None => InternalServerError(Json.obj("message" -> "Failed to create association"))
        }
      }
    )
  }

  // Delete Routes
  def deleteUser(id: String) = Action.async {
    User.delete(Identity[User](id)).map {
      case 1 => Ok(Json.obj("message" -> "User deleted successfully"))
      case 0 => NotFound(Json.obj("message" -> "User not found"))
      case _ => InternalServerError(Json.obj("message" -> "Failed to delete user"))
    }
  }

  def deleteClique(id: String) = Action.async {
    Clique.delete(Identity[Clique](id)).map {
      case 1 => Ok(Json.obj("message" -> "Clique deleted successfully"))
      case 0 => NotFound(Json.obj("message" -> "Clique not found"))
      case _ => InternalServerError(Json.obj("message" -> "Failed to delete clique"))
    }
  }

  def deleteCluster(id: String) = Action.async {
    Cluster.delete(Identity[Cluster](id)).map {
      case 1 => Ok(Json.obj("message" -> "Cluster deleted successfully"))
      case 0 => NotFound(Json.obj("message" -> "Cluster not found"))
      case _ => InternalServerError(Json.obj("message" -> "Failed to delete cluster"))
    }
  }

  // Update Routes
  def updateUser(id: String) = Action.async(parse.json) { request =>
    val userResult = request.body.validate[UserConnection]
    userResult.fold(
      errors => Future.successful(BadRequest(Json.obj("message" -> JsError.toJson(errors)))),
      user => {
        User.findById(Identity[User](id)).flatMap {
          case Some(existingUser) =>
            // Update user fields
            Future.successful(Ok(Json.obj("message" -> "User updated successfully")))
          case None => Future.successful(NotFound(Json.obj("message" -> "User not found")))
        }
      }
    )
  }

  def updateCluster(id: String) = Action.async(parse.json) { request =>
    val clusterResult = request.body.validate[ClusterConnection]
    clusterResult.fold(
      errors => Future.successful(BadRequest(Json.obj("message" -> JsError.toJson(errors)))),
      cluster => {
        Cluster.findById(Identity[Cluster](id)).flatMap {
          case Some(existingCluster) =>
            existingCluster.updateTitle(cluster.title).map {
              case 1 => Ok(Json.obj("message" -> "Cluster updated successfully"))
              case _ => InternalServerError(Json.obj("message" -> "Failed to update cluster"))
            }
          case None => Future.successful(NotFound(Json.obj("message" -> "Cluster not found")))
        }
      }
    )
  }

  // List Routes
  def listUsers = Action.async { request =>
    val start = request.getQueryString("start").map(_.toInt).getOrElse(0)
    val count = request.getQueryString("count").map(_.toInt).getOrElse(10)
    
    User.table.drop(start).take(count).result.map { users =>
      Ok(Json.toJson(users))
    }
  }

  def listClusters = Action.async { request =>
    val start = request.getQueryString("start").map(_.toInt).getOrElse(0)
    val count = request.getQueryString("count").map(_.toInt).getOrElse(10)
    
    Cluster.table.drop(start).take(count).result.map { clusters =>
      Ok(Json.toJson(clusters))
    }
  }

  def listPosts = Action.async { request =>
    val start = request.getQueryString("start").map(_.toInt).getOrElse(0)
    val count = request.getQueryString("count").map(_.toInt).getOrElse(10)
    
    Post.table.drop(start).take(count).result.map { posts =>
      Ok(Json.toJson(posts))
    }
  }

  // Search Routes
  def searchUsers = Action.async(parse.json) { request =>
    val searchTerm = (request.body \ "query").as[String]
    User.table.filter(_.username like s"%$searchTerm%").result.map { users =>
      Ok(Json.toJson(users))
    }
  }

  def searchClusters = Action.async(parse.json) { request =>
    val searchTerm = (request.body \ "query").as[String]
    Cluster.table.filter(_.title like s"%$searchTerm%").result.map { clusters =>
      Ok(Json.toJson(clusters))
    }
  }

  def searchPosts = Action.async(parse.json) { request =>
    val searchTerm = (request.body \ "query").as[String]
    Post.table.join(Comment.table).on(_.caption === _.id)
      .filter(_._2.content like s"%$searchTerm%")
      .map(_._1)
      .result.map { posts =>
        Ok(Json.toJson(posts))
      }
  }
}