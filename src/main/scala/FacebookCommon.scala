import spray.routing.RequestContext
import scala.collection.immutable.HashMap
import spray.json._
import DefaultJsonProtocol._
import scala.collection.mutable.ListBuffer
//import org.specs2.mutable.Specification
import spray.httpx.SprayJsonSupport._
import nodeTypes._

object FacebookCommon {
  val server:String="localhost"
  val port:Int=1456
  val address:String="http://"+server+":"+port.toString()
  
case class Person(name: Map[String,String])
case class CasePost (postId:Int,post:String,timeStamp:String)
case class CaseComment(commentId:Int,nodeId:Int,comment:String)
case class CaseUser(userId:String,firstName:String,lastName:String,DOB:String,gender:String,email:String)
   /* friendList:Option[HashMap[String,String]]=None,postsOfUser:Option[HashMap[String,String]]=None,statusUpdates:Option[HashMap[String,String]]=None,
    photosOfUser:Option[HashMap[String,String]]=None,pagesOfUser:Option[HashMap[String,String]]=None)*/

case class processPostOfUser(reqCtx:RequestContext,byUser:String,ofUser:String,newCasePost:CasePost)
case class getPostOfUser(requestContext:RequestContext,ofUser:String)
case class getPostIdsOfUser(reqCtx:RequestContext,ofUser:String)
case class commentOnPostOfUser(requestContext:RequestContext,byUser:String,ofUser:String,caseComment:CaseComment)

case class commentOnPage(requestContext:RequestContext,byUser:String,ofUser:String,comment:Comment)

case class friendRequest(reqCtx:RequestContext,byUser:String,toUser:String)
case class acceptRejectFriendRequest(reqCtx:RequestContext,byUser:String,toUser:String,acceptOrReject:String)
case class getUserProfileAndInfo(reqCtx:RequestContext,byUser:String,ofUser:String)
case class getUserProfileAndInfoClient(ofUser:String)
case class registerNewUser(reqCtx:RequestContext,newUserInfo:CaseUser)
case class registerNewUserClient(newUserInfo:CaseUser)
case class sendFriendRequest(toUser:String)
case class respondToFriendRequest(ofUser:String,acceptOrReject:String)

case class updateStatusClient(post:String,timeOfPost:String)
case class getPostsOfUserAndCommentClient(ofUser:String)
case class sendPostToUser(toUser:String,post:String,timeOfPost:String)
case class commentOnPost(ofUser:String,postId:Int,comment:String)



case class CasePage(pageId:String,pageOwner:String,userList:Option[Map[String,String]]=None,commentList:Option[Map[String,String]]=None,picList:Option[Map[String,String]]=None,
                    likeList:Option[Map[String,String]]=None)
case class postOnPage(pageId:Int,casePost:CasePost)
case class postPostOnPage(reqCtx:RequestContext,byUser:String,pageId:String,casePostOnPage:CasePost)
case class commentOnNodeOfPage(requestContext:RequestContext,byUser:String,nodeType:String,pageId:String,nodeId:Int,caseCommentServer:CaseComment)
case class comentOnNodeOfPageClient(pageId:Int,nodeId:Int,nodeType:String,casePostOnPage:CaseComment)
case class postPostOnPageClient(pageId:String,casePostOnPage:CasePost)
case class createNewPageClient(pageId:String,caseNewPage:CasePage)
case class createNewPage(reqCtx:RequestContext,newCasePage:CasePage,byUser:String,pageId:String)
case class getPagesAndPostOnPageClient()
case class getPagesAndJoinUserToPage(userName:String)
case class joinUserToPageClient(userName:String,randomPageId:String)
case class getAllPages(requestContext:RequestContext,byUser:String)
case class addUserToPage(requestContext:RequestContext,user:String,pageId:String)
case class getPageInfo()

case class CasePhoto(photoId:Int,photo:String,ownerId:String)
case class CaseAlbum(albumId:Int,albumName:String,photoMap:Map[Int,CasePhoto])
case class postPicToUser(toUser:String)
case class createNewAlbumServer(reqCtx:RequestContext,ofUser:String,albumId:Int)
case class addPhotoToAlbum(requestContext:RequestContext,ofUser:String,albumId:Int,newPic:String)
case class getAlbumsIdsOfUser(requestContext:RequestContext,ofUser:String)
case class getAlbumsOfUser(requestContext:RequestContext,ofUser:String)
case class postPhotoToUser(reqCtx:RequestContext,fromUser:String,toUser:String,newPic:String)
case class createNewAlbumClient(ofUser:String)
case class getAlbumsOfUserClient(ofUser:String)
case class getAlbumsAndAddPhotoToAlbumClient()
case class addPhotoToAlbumClient(userName:String,albumId:Int)
case class postPicToUserClient(toUser:String)

object CasePage extends DefaultJsonProtocol{
    implicit val imp=jsonFormat6(CasePage.apply)
  }
   
object Person extends DefaultJsonProtocol {
  implicit val impPerson = jsonFormat1(Person.apply)
}
  

object CasePost extends DefaultJsonProtocol {
  implicit val impCase = jsonFormat3(CasePost.apply)
}

object CaseUser extends DefaultJsonProtocol {
  implicit val impUser = jsonFormat6(CaseUser.apply)
}

object CaseComment extends DefaultJsonProtocol {
  implicit val impUser = jsonFormat3(CaseComment.apply)
}

object CasePhoto extends DefaultJsonProtocol {
  implicit val impUser = jsonFormat3(CasePhoto.apply)
}

object CaseAlbum extends DefaultJsonProtocol {
  implicit val impUser = jsonFormat3(CaseAlbum.apply)
}

/*object CasePage extends DefaultJsonProtocol {
  implicit val impUser = jsonFormat5(CasePage.apply)
}*/

}