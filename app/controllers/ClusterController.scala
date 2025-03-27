package controllers

import javax.inject._
import play.api.mvc._
import slick.jdbc.MySQLProfile

import scala.concurrent.ExecutionContext

@Singleton
class ClusterController @Inject()(val controllerComponents: ControllerComponents) extends BaseController {



  def getUserFace(user_id: String) : Action[AnyContent] = Action {

  }

  def getUserIteraction(obj: String, subj: String) : Action[AnyContent] = Action {
  }
}