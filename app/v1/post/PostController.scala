package v1.post

import javax.inject.Inject

import play.api.libs.json._
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

case class PostFormInput(title: String, body: String)

/**
  * Takes HTTP requests and produces JSON.
  */
class PostController @Inject()(handler: PostResourceHandler)(implicit ec: ExecutionContext) extends Controller {

  private val logger = org.slf4j.LoggerFactory.getLogger(this.getClass)

  def index: Action[AnyContent] = Action.async { request =>
      handler.find.map { posts =>
        Ok(Json.toJson(posts))
      }
  }

  def process: Action[AnyContent] = Action.async { request =>
    request.body.asJson match {
      case Some(data) =>
        data match {
        case JsObject(map) =>
          var input = PostFormInput(
            map("title").toString(),
            map("body").toString())
          handler.create(input).map(post => {
            Created(Json.toJson(post)).withHeaders(LOCATION -> post.link)
          })
        case _ => Future.successful(BadRequest)
      }
      case None => Future.successful(BadRequest)
    }
  }

  def show(id: String): Action[AnyContent] = Action.async { request =>
      handler.lookup(id).map { post =>
        Ok(Json.toJson(post))
      }
  }
}
