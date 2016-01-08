import com.typesafe.config.ConfigFactory
import akka.actor.{Actor,ActorSystem}
import spray.http._
import spray.client.pipelining._
import scala.concurrent.Future
import scala.util.{Success, Failure}
import spray.json.DefaultJsonProtocol
import spray.json._
import DefaultJsonProtocol._
import spray.httpx.SprayJsonSupport._
import spray.routing.RequestContext
import org.json4s._
import scala.util.Random
import org.json4s.jackson.JsonMethods._
import org.json4s.jackson.Serialization
import org.json4s.jackson.Serialization.{read, write}
import org.json4s.DefaultFormats
import FacebookCommon._


object FaceBookClientClasses{
      
implicit val system = ActorSystem()
import system.dispatcher // execution context for futures
implicit val formats = Serialization.formats(NoTypeHints)

val pipeline: HttpRequest => Future[HttpResponse] = sendReceive

  
class User extends Actor{
  
var userName:String=new String()





def receive={
  case getUserProfileAndInfoClient(ofUser:String)=>{
    println(FacebookCommon.address+"/"+userName+"/"+ofUser+"/userInfo")
    val userInfoResponse: Future[HttpResponse] = pipeline(Get(FacebookCommon.address+"/"+userName+"/"+ofUser+"/userInfo"))
    userInfoResponse.onComplete{
      case Success(httpResponse)=>{
        if(httpResponse.status.isSuccess){
        //println("Info of user "+ofUser+" as requested by "+userName+":")
        println("\n "+httpResponse.entity.asString)
        }
        else{
          //println("Retrieval of Info of user "+ofUser+" as requested by "+userName+" failed with error message")
          println(httpResponse.message)
        }
      }
      case Failure(f)=>{
        println("Retrieval of Info of user "+ofUser+" as requested by "+userName+" failed with error message")
        println(f.getMessage)
      }
    }
  }
  
  case registerNewUserClient(newUserInfo:CaseUser)=>{
    println("Got the register user message with path "+FacebookCommon.address+"/registerNewUser")
    val registerNewUserResponse: Future[HttpResponse] = pipeline(Post(FacebookCommon.address+"/registerNewUser",newUserInfo))
    registerNewUserResponse.onComplete{
      case Success(httpResponse)=>{
        if(httpResponse.status.isSuccess){
          userName=newUserInfo.userId
          //println("New User "+newUserInfo.firstName+" "+newUserInfo.lastName+" added" )
          println(httpResponse.entity.asString)
        }
        else{
           //println("Failed to added new User "+newUserInfo.firstName+" "+newUserInfo.lastName+". Failed with error message : " )
            println(httpResponse.entity.asString)
        }
      }
      case Failure(f)=>{
        println("Failed to added new User "+newUserInfo.firstName+" "+newUserInfo.lastName+". Failed with error message : " )
        println(f.getMessage)
      }
    }
  }

  case  sendFriendRequest(toUser:String)=>{
    println("Got the request for a friend request with the path"+FacebookCommon.address+"/"+userName+"/"+toUser+"/friendRequest")
    val userFriendRequestFuture:Future[HttpResponse]=pipeline(Post(FacebookCommon.address+"/"+userName+"/"+toUser+"/friendRequest"))
    userFriendRequestFuture.onComplete {
      case Success(httpResponse)=>{
        if(httpResponse.status.isSuccess){
        //println("Friend Request sent to "+toUser+" from "+userName+":")
        println("\n "+httpResponse.entity.asString)
        }
        else{
          //println("Friend Request to "+toUser+" from "+userName+" failed with error message")
          println(httpResponse.entity.asString)
        }
      }
      case Failure(f)=>{
        println("Friend Request to "+toUser+" from "+userName+" failed with error message")
        println(f.getMessage)
      }
    }
    
  }

  case respondToFriendRequest(ofUser:String,acceptOrReject:String)=>{
    val respondToRequestFuture:Future[HttpResponse]=pipeline(Post(FacebookCommon.address+"/"+userName+"/"+ofUser+"/acceptOrReject").withEntity(acceptOrReject))
    respondToRequestFuture.onComplete {
      case Success(httpResponse)=>{
        if(httpResponse.status.isSuccess){
          //println("Added the friend successfully")
          println(httpResponse.entity.asString)
        }
        else{
        //  println("Failed to process Friend Request of "+ofUser+" by "+userName+". Failed with error message")
          println(httpResponse.entity.asString)
        }
      }
      case Failure(f)=>{
        println("Failed to process Friend Request of "+ofUser+" by "+userName+". Failed with error message")
        println(f.getMessage)
      }
    }
    
}

  case getPostsOfUserAndCommentClient(ofUser:String)=>{
    val getPostsOfUserFuture: Future[HttpResponse] = pipeline(Get(FacebookCommon.address+"/"+userName+"/"+ofUser+"/getPostIds"))
    getPostsOfUserFuture.onComplete{
      case Success(httpResponse)=>{
        if(httpResponse.status.isSuccess){
          
        //  read[Array[CaseComment]](httpResponse.entity.asString)
        
        println("\n posts of user"+httpResponse.entity.asString)
        var postsOfUserIds:List[Int]=parse(httpResponse.entity.asString).extract[List[Int]]
        if(!postsOfUserIds.isEmpty){
          var randomPostId=postsOfUserIds(Random.nextInt(postsOfUserIds.size))
          self!commentOnPost(ofUser:String,randomPostId,"comment"+randomPostId)
        }
        }
        else{  
          println("Retrieval of posts of user "+ofUser+" as requested by "+userName+" failed with error message")
          println(httpResponse.entity.asString)
        }
      }
      case Failure(f)=>{
        println("Retrieval of posts of user "+ofUser+" as requested by "+userName+" failed with error message")
        println(f.getMessage)
      }
    }
}

  case sendPostToUser(toUser:String,post:String,timeOfPost:String)=>{
    val newPostToUser=new CasePost(0,post,timeOfPost)//.toJson
    val sendPostToUserFuture: Future[HttpResponse] = pipeline(Post(FacebookCommon.address+"/"+userName+"/"+toUser+"/post",newPostToUser))//.asJsObject))

    sendPostToUserFuture.onComplete{
      
      case Success(httpResponse)=>{
        if(httpResponse.status.isSuccess){
       // println("Post sent to user "+toUser+" by "+userName+" successfully posted")
          println(httpResponse.entity.asString)
        }
        else{
         // println("Post sent to user "+toUser+" by "+userName+" failed with error message")
          println(httpResponse.entity.asString)
        }
      }
      case Failure(f)=>{
        println("Post sent to user "+toUser+" by "+userName+" failed with error message")
        println(f.getMessage)
      }
    }
  }
  
  case updateStatusClient(post:String,timeOfPost:String)=>{
    val newPostToUser=new CasePost(0,post,timeOfPost)
    val sendPostToUserFuture: Future[HttpResponse] = pipeline(Post(FacebookCommon.address+"/"+userName+"/"+userName+"/updateStatus",newPostToUser))

    sendPostToUserFuture.onComplete{
      
      case Success(httpResponse)=>{
        if(httpResponse.status.isSuccess){
       // println("Post sent to user "+toUser+" by "+userName+" successfully posted")
          println(httpResponse.entity.asString)
        }
        else{
         // println("Post sent to user "+toUser+" by "+userName+" failed with error message")
          println(httpResponse.entity.asString)
        }
      }
      case Failure(f)=>{
        println("Status update of "+userName+" failed with error message")
        println(f.getMessage)
      }
    }
  }
  
  case commentOnPost(ofUser:String,postId:Int,comment:String)=>{
    val commentOnPostFuture: Future[HttpResponse] = pipeline(Post(FacebookCommon.address+"/"+userName+"/"+ofUser+"/commentOnPost",new CaseComment(0,postId,comment)))
    commentOnPostFuture.onComplete{
      case Success(httpResponse)=>{
        if(httpResponse.status.isSuccess){
          println(httpResponse.entity.asString)
        }
        else{
          println(httpResponse.entity.asString)
        }
      }
      case Failure(f)=>{
        println("Comment sent to user "+ofUser+"'s post by "+userName+" failed with error message")
        println(f.getMessage)
      }
    }
    
  }
  
  //case createNewPage()=>
    
  
  case getPageInfo()=>
    
  case getPagesAndJoinUserToPage(userName:String)=>
    val getAllPagesFuture: Future[HttpResponse] = pipeline(Get(FacebookCommon.address+"/"+userName+"/"+"getAllPages"))
    getAllPagesFuture.onComplete{
      case Success(httpResponse)=>{
        if(httpResponse.status.isSuccess){
          println("blah blah")
          println(httpResponse.entity.asString)
          var allPageIdList:List[String]=parse(httpResponse.entity.asString).extract[List[String]]
          if(!allPageIdList.isEmpty){
            var randomPageId=allPageIdList(Random.nextInt(allPageIdList.size))
            self!joinUserToPageClient(userName,randomPageId)
          }
          
        }
        else{
          println(httpResponse.entity.asString)
        }
      }
      case Failure(f)=>{
        println("Get all pages requested by "+userName+" failed with error message")
        println(f.getMessage)
      }
    }
  
  case joinUserToPageClient(userName,randomPageId)=>
    val joinUserToPageFuture: Future[HttpResponse] = pipeline(Post(FacebookCommon.address+"/"+userName+"/"+randomPageId+"/addUserToPage"))
    joinUserToPageFuture.onComplete{
      case Success(httpResponse)=>{
        if(httpResponse.status.isSuccess){
          println(httpResponse.entity.asString)
          }
          else{
          println(httpResponse.entity.asString)
        }
      }
      case Failure(f)=>{
        println("Get all pages requested by "+userName+" failed with error message")
        println(f.getMessage)
      }
    }
  
  
  case getPagesAndPostOnPageClient()=>
    var randomCasePost=new CasePost(0,"post"+System.currentTimeMillis(),"timeStamp") 
    val getPagesFuture: Future[HttpResponse] = pipeline(Get(FacebookCommon.address+"/"+userName+"/"+"getAllPages"))
    getPagesFuture.onComplete{
      case Success(httpResponse)=>{
        if(httpResponse.status.isSuccess){
          println(httpResponse.entity.asString)
          var allPageIdList:List[String]=parse(httpResponse.entity.asString).extract[List[String]]
          if(!allPageIdList.isEmpty){
            var randomPageId=allPageIdList(Random.nextInt(allPageIdList.size))
            self!postPostOnPageClient(randomPageId,randomCasePost)
          }
          
        }
        else{
          println(httpResponse.entity.asString)
        }
      }
      case Failure(f)=>{
        println("Get all pages requested by "+userName+" failed with error message")
        println(f.getMessage)
      }
    }
  
    
    
    
  case postPostOnPageClient(pageId:String,casePostOnPage:CasePost)=>
    val postOnPageFuture: Future[HttpResponse] = pipeline(Post(FacebookCommon.address+"/"+userName+"/"+pageId+"/postOnPage",casePostOnPage))
    postOnPageFuture.onComplete{
      case Success(httpResponse)=>{
        if(httpResponse.status.isSuccess){
          println(httpResponse.entity.asString)
        }
        else{
          println(httpResponse.entity.asString)
        }
      }
      case Failure(f)=>{
        println("Post of "+userName+" on the page failed with error message")
        println(f.getMessage)
      }
    }
    
   case comentOnNodeOfPageClient(pageId:Int,nodeId:Int,nodeType:String,caseCommentOnPage:CaseComment)=>
    val postOnPageFuture: Future[HttpResponse] = pipeline(Post(FacebookCommon.address+"/"+userName+"/"+pageId+"/"+nodeId+"/"+nodeType,caseCommentOnPage))
    postOnPageFuture.onComplete{
      case Success(httpResponse)=>{
        if(httpResponse.status.isSuccess){
          println(httpResponse.entity.asString)
        }
        else{
          println(httpResponse.entity.asString)
        }
      }
      case Failure(f)=>{
        println("Post of "+userName+" on the page failed with error message")
        println(f.getMessage)
      }
    }
    
   case createNewPageClient(pageId:String,caseNewPage:CasePage)=>
     val createNewPageFuture: Future[HttpResponse] = pipeline(Post(FacebookCommon.address+"/"+userName+"/"+pageId+"/openNewPage",caseNewPage))
      createNewPageFuture.onComplete{
      case Success(httpResponse)=>{
        if(httpResponse.status.isSuccess){
          println(httpResponse.entity.asString)
        }
        else{
          println(httpResponse.entity.asString)
        }
      }
      case Failure(f)=>{
        println("Create new Page "+pageId+" failed with error message")
        println(f.getMessage)
      }
    }

  
  case createNewAlbumClient(ofUser)=>{
    var albumName=new String()
    val createNewAlbumFuture: Future[HttpResponse] = pipeline(Post(FacebookCommon.address+"/"+userName+"/createNewAlbum",albumName))
      createNewAlbumFuture.onComplete{
      case Success(httpResponse)=>{
        if(httpResponse.status.isSuccess){
          println(httpResponse.entity.asString)
        }
        else{
          println(httpResponse.entity.asString)
        }
      }
      case Failure(f)=>{
        println("Creation of new album "+albumName+" by user "+userName+" failed with error message")
        println(f.getMessage)
      }
    }
  }
  
  case getAlbumsOfUserClient(ofUser:String)=>
    val createNewAlbumFuture: Future[HttpResponse] = pipeline(Get(FacebookCommon.address+"/"+ofUser+"/getAlbums"))
      createNewAlbumFuture.onComplete{
      case Success(httpResponse)=>{
        if(httpResponse.status.isSuccess){
          println(httpResponse.entity.asString)
        }
        else{
          println(httpResponse.entity.asString)
        }
      }
      case Failure(f)=>{
        println("Get albums of "+ofUser+" failed with error message")
        println(f.getMessage)
      }
    }
    
  case getAlbumsAndAddPhotoToAlbumClient()=>
     val addPhotoToAlbumFuture: Future[HttpResponse]=pipeline(Get(FacebookCommon.address+"/"+userName+"/getAlbumsIdsOfUser"))
      addPhotoToAlbumFuture.onComplete{
      case Success(httpResponse)=>{
        
        if(httpResponse.status.isSuccess){
          println(httpResponse.entity.asString)
          
          var listOfAlbumIds:List[Int]=parse(httpResponse.entity.asString).extract[List[Int]]
          if(!listOfAlbumIds.isEmpty){
            var randomAlbumId=listOfAlbumIds(Random.nextInt(listOfAlbumIds.size))
            self!addPhotoToAlbumClient(userName,randomAlbumId)
          }
          else{
            println("No Albums exist for this user yet.")
          }
        
        }
        else{
          println("Getting Albums of user "+userName+" failed with error message")
          println(httpResponse.entity.asString)
        }
      }
      case Failure(f)=>{
        println("Getting Albums of user "+userName+" failed with error message")
        println(f.getMessage)
      }
    }
     
  case addPhotoToAlbumClient(userName:String,albumId:Int)=>
    
   /* val is = this.getClass.getClassLoader().getResourceAsStream("picture.jpg");
      val stream = Stream.continually(is.read).takeWhile(_ != -1).map(_.toByte)
      val bytes = stream.toArray
      val pictureBase64String = new sun.misc.BASE64Encoder().encode(bytes)*/
    val pictureBase64String="pictureBase64String"
      
    val addPhotoToAlbumFuture: Future[HttpResponse] = pipeline(Post(FacebookCommon.address+"/"+userName+"/"+albumId+"/addPhotoToAlbum",pictureBase64String))
    addPhotoToAlbumFuture.onComplete{
      case Success(httpResponse)=>{
        
        if(httpResponse.status.isSuccess){
          println(httpResponse.entity.asString)
        }
        else{
          println("Add Photo to Album failed with error message")
          println(httpResponse.entity.asString)
        }
      }
      case Failure(f)=>{
        println("Adding photo to album "+albumId+" of user "+userName+" failed with error message")
        println(f.getMessage)
      }
    }
    
  case postPicToUserClient(toUser:String)=>
    /*  val is = this.getClass.getClassLoader().getResourceAsStream("picture.jpg")
      val stream = Stream.continually(is.read).takeWhile(_ != -1).map(_.toByte)
      val bytes = stream.toArray*/
     // val pictureBase64String = new sun.misc.BASE64Encoder().encode(bytes)
    val pictureBase64String="picture.jpg"
      
      val addPhotoToAlbumFuture: Future[HttpResponse] = pipeline(Post(FacebookCommon.address+"/"+userName+"/"+toUser+"/postPicToUser",pictureBase64String))
      addPhotoToAlbumFuture.onComplete{
      case Success(httpResponse)=>{
        if(httpResponse.status.isSuccess){
          println(httpResponse.entity.asString)
        }
        else{
          println(httpResponse.entity.asString)
        }
      }
      case Failure(f)=>{
        println(userName+" tried to post photo to "+toUser+" of user "+userName+" but failed with error message")
        println(f.getMessage)
      }
    }
      
}



    
}      
    
  
}