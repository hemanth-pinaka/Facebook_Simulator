import com.typesafe.config.ConfigFactory
import akka.actor.{ActorSystem, Props}
import akka.io.IO
import akka.actor.Actor
import spray.routing.HttpService
import spray.can.Http
import spray.json._
import DefaultJsonProtocol._ 
import spray.json.DefaultJsonProtocol
import scala.concurrent.duration._
import akka.util.Timeout
import akka.actor._
import spray.can.server.Stats
import spray.routing.{RequestContext,Rejection}
import spray.can.Http.RegisterChunkHandler
import spray.httpx.marshalling.Marshaller
import scala.collection.immutable.HashMap
import spray.routing.RoutingSettings
import spray.http.StatusCodes
import FacebookCommon._



import nodeTypes._

 object ReceiveHttp extends App{

    
  implicit val serverActorSystem=ActorSystem("FB")
  
  val receiverActor=serverActorSystem.actorOf(Props[FaceBookServer], "receiverRequests")
  IO(Http)! Http.Bind(receiverActor,interface = FacebookCommon.server, port = FacebookCommon.port)
  
  
  var userBase:Map[String,UserClass]=new HashMap[String,UserClass]()
  var pages:Map[String,Page]=new HashMap[String,Page]()
 
  

  object seqCounters{
    var postSeq:Int=0
    var commentSeq:Int=0
    var albumSeq:Int=0
    var picSeq:Int=0
  }
  
  class FaceBookServer extends Actor with fbPostTrait with fbUserTrait with fbPageTrait with fbPicTrait{
    
     def actorRefFactory = context
     implicit val rSettings = RoutingSettings.default(context)
     
     
     
    def receive = runRoute(receivePathPost
                           ~receivePathPic
                           ~receivePathUser
                           ~receivePagePath)
    }
  
  
   
  
  trait fbPostTrait extends HttpService{
    import spray.httpx.SprayJsonSupport._
    import spray.http.MediaTypes
    val receivePathPost={
      path(Segment/Segment/"posts"){(byUser,ofUser)=>
        requestContext =>
          val postActor = serverActorSystem.actorOf(Props(new PostActor()))
          postActor!getPostOfUser(requestContext,ofUser)
          postActor!PoisonPill        
     }~
     path(Segment/Segment/"getPostIds"){(byUser,ofUser)=>
        requestContext =>
          val postActor = serverActorSystem.actorOf(Props(new PostActor()))
          postActor!getPostIdsOfUser(requestContext,ofUser)
          postActor!PoisonPill        
     }~
    post{
      path(Segment/Segment/"post"){(byUser,toUser)=>
        entity(as[CasePost]){ newPostForUser =>
          requestContext =>
            val postActor = serverActorSystem.actorOf(Props(new PostActor()))
            postActor!processPostOfUser(requestContext,byUser,toUser,newPostForUser)
            postActor!PoisonPill
        }
        
      }~
      path(Segment/Segment/"updateStatus"){(byUser,toUser)=>
        entity(as[CasePost]){ newPostForUser =>
          requestContext =>
            val postActor = serverActorSystem.actorOf(Props(new PostActor()))
            postActor!processPostOfUser(requestContext,byUser,toUser,newPostForUser)
            postActor!PoisonPill
        }
        
      }~
      path(Segment/Segment/"commentOnPost"){(byUser,ofUser)=>
        entity(as[CaseComment]){ commentOnPost=>
          requestContext =>
            val postActor = serverActorSystem.actorOf(Props(new PostActor()))
            postActor!commentOnPostOfUser(requestContext,byUser,ofUser,commentOnPost)
            postActor!PoisonPill
        }
      }
    }
    }
  
  }
 
  
  trait fbPicTrait extends HttpService{
    import spray.http.MediaTypes
    import spray.httpx.SprayJsonSupport._
    val receivePathPic={
      get{
      path(Segment/"getAlbums"){(ofUser)=>
      requestContext=>
         val picActor = serverActorSystem.actorOf(Props(new PhotoActor()))
         picActor!getAlbumsOfUser(requestContext,ofUser)
         picActor!PoisonPill
     }~
     path(Segment/"getAlbumsIdsOfUser"){(ofUser)=>
      requestContext=>
         val picActor = serverActorSystem.actorOf(Props(new PhotoActor()))
         picActor!getAlbumsIdsOfUser(requestContext,ofUser)
         picActor!PoisonPill
     }
    }~
     post{
      path(Segment/"createNewAlbum"){(ofUser)=>
      requestContext=>
         val picActor = serverActorSystem.actorOf(Props(new PhotoActor()))
         seqCounters.albumSeq=seqCounters.albumSeq+1
         picActor!createNewAlbumServer(requestContext,ofUser,seqCounters.albumSeq)
         picActor!PoisonPill
     }~
      path(Segment/IntNumber/"addPhotoToAlbum"){(ofUser,albumId)=>
        entity(as[String]){ newPic =>
          requestContext=>
         val picActor = serverActorSystem.actorOf(Props(new PhotoActor()))
         picActor!addPhotoToAlbum(requestContext,ofUser,albumId,newPic)
         picActor!PoisonPill
         }
     }~
      path(Segment/Segment/"postPicToUser"){(fromUser,toUser)=>
        entity(as[String]){ newPic =>
        requestContext=>
        val picActor = serverActorSystem.actorOf(Props(new PhotoActor()))
         picActor!postPhotoToUser(requestContext,fromUser,toUser,newPic)
         picActor!PoisonPill
        }
       }
      }
      
  }
}
  
trait fbUserTrait extends HttpService{
  
  import spray.httpx.SprayJsonSupport._
  val receivePathUser={
    get{
      path(Segment/Segment/"userInfo"){(byUser,ofUser)=>
        requestContext =>
          val userActor = serverActorSystem.actorOf(Props(new UserActor()))
          userActor!getUserProfileAndInfo(requestContext,byUser,ofUser)
          userActor!PoisonPill
      }
    }~
    post{
      path("registerNewUser"){
        entity(as[CaseUser]){ newRegisterUser =>
          requestContext=>
            val userActor = serverActorSystem.actorOf(Props(new UserActor()))
            userActor!registerNewUser(requestContext,newRegisterUser)
            userActor!PoisonPill
        }
      }~  
      path(Segment/Segment/"friendRequest"){(byUser,toUser)=>
        requestContext =>
        val userActor = serverActorSystem.actorOf(Props(new UserActor()))
        userActor!friendRequest(requestContext,byUser,toUser)
        userActor!PoisonPill
      }~
      path(Segment/Segment/"acceptOrReject"){(byUser,ofUser)=>
        entity(as[String]){acceptOrReject=>
        requestContext =>
          val userActor = serverActorSystem.actorOf(Props(new UserActor()))
          userActor!acceptRejectFriendRequest(requestContext,byUser,ofUser,acceptOrReject)
          userActor!PoisonPill
        }
     }
    }
  }
}

trait fbPageTrait extends HttpService{
  import spray.httpx.SprayJsonSupport._
  
     val receivePagePath={
       post{
         path(Segment/Segment/"openNewPage"){(byUser,pageId)=>
           entity(as[CasePage]){newCasePage=>
             requestContext =>
               val pageActor = serverActorSystem.actorOf(Props(new PageActor()))
               pageActor!createNewPage(requestContext,newCasePage,byUser,pageId)
               pageActor!PoisonPill
            }
           }~
         path(Segment/Segment/IntNumber/Segment/"commentOnPostOfPage"){(byUser,pageId,nodeId,nodeType)=>
          entity(as[CaseComment]){ commOnNodeOfPage=>
          requestContext =>
              val pageActor = serverActorSystem.actorOf(Props(new PageActor()))
              pageActor!commentOnNodeOfPage(requestContext,byUser,nodeType,pageId,nodeId,commOnNodeOfPage)
              pageActor!PoisonPill
            }
          }~
         path(Segment/Segment/"postOnPage"){(byUser,pageId)=>
           entity(as[CasePost]){casePostOnPage=>
             requestContext=>
               val pageActor = serverActorSystem.actorOf(Props(new PageActor()))
               pageActor!postPostOnPage(requestContext,byUser,pageId,casePostOnPage)
               pageActor!PoisonPill
           }
         }~
         path(Segment/Segment/"addUserToPage"){(user,pageId)=>
             requestContext=>
               val pageActor = serverActorSystem.actorOf(Props(new PageActor()))
               pageActor!addUserToPage(requestContext,user,pageId)
               pageActor!PoisonPill
           
         }
        }~
       get{
         path(Segment/"getAllPages"){(byUser)=>
                 requestContext=>
               val pageActor = serverActorSystem.actorOf(Props(new PageActor()))
               pageActor!getAllPages(requestContext,byUser)
               pageActor!PoisonPill
         }
       }
  }
 }
    


class PostActor extends Actor{
  import spray.http.MediaTypes
  import spray.httpx.SprayJsonSupport._
  def receive={
    case getPostOfUser(reqCtx:RequestContext,ofUser:String)=>
     
            var postMap:Map[Int,Post]=userBase(ofUser).postsOfUser
            var returnPostMap:Map[Int,CasePost]=new HashMap[Int,CasePost]
            
            for ((k,v) <- postMap){
              var varCP=new CasePost(v.postId,v.post,v.timeStamp)
              returnPostMap+=(k->varCP)
            }
            reqCtx.complete(returnPostMap)
            
    case getPostIdsOfUser(reqCtx:RequestContext,ofUser:String)=>
     
            var postMap:Map[Int,Post]=userBase(ofUser).postsOfUser
            
           /* var returnPostMap:Map[Int,CasePost]=new HashMap[Int,CasePost]
            
            for ((k,v) <- postMap){
              var varCP=new CasePost(v.postId,v.post,v.timeStamp)
              returnPostMap+=(k->varCP)
            }
            
            println(returnPostMap.toJson.toString())*/
            
            reqCtx.complete(postMap.keys.toList)

            
    case processPostOfUser(reqCtx:RequestContext,byUser:String,ofUser:String,newCasePost:CasePost)=>
          seqCounters.postSeq=seqCounters.postSeq+1
          var newPost:Post=new Post(seqCounters.postSeq,newCasePost.post,newCasePost.timeStamp)
          if(!byUser.equalsIgnoreCase(ofUser)){
            var userClass=userBase.get(ofUser).orElse(userBase.get("userId0"))
           userClass.get.postsOfUser+=(newPost.postId->newPost)
          reqCtx.complete("WALL POST \nMessage "+newCasePost.post+" posted successfully on "+ofUser+"'s wall by "+byUser)
          }
          else{
            var user=userBase.get(ofUser).orElse(userBase.get("userId0"))
            user.get.statusUpdates+=(newCasePost.postId->newPost)
            reqCtx.complete("STATUS UPDATE \nStatus updated successfully updated by "+byUser+" with status "+newCasePost.post+"\n")
          }
          
          
          
    case commentOnPostOfUser(reqCtx:RequestContext,byUser,ofUser,caseComment)=>
      seqCounters.commentSeq=seqCounters.commentSeq+1
      var commentServer:Comment=new Comment(seqCounters.commentSeq,caseComment.nodeId,caseComment.comment)
      var user=userBase.get(ofUser).orElse(userBase.get("userId0"))
      user.get.postsOfUser.get(commentServer.nodeId).get.commentsOfPost+=(seqCounters.commentSeq->commentServer)
      reqCtx.complete("COMMENT ON POST OF USER\n"+byUser +" Successfully commented on the post of "+ofUser)
    }
  }

class UserActor extends Actor{
  
  def receive={
    
    case registerNewUser(reqCtx:RequestContext,passedCaseUser:CaseUser)=>
      
          if(userBase.contains(passedCaseUser.userId)){
            reqCtx.complete(StatusCodes.Conflict,passedCaseUser.userId+" is already a user")
          }
          else{
          var newUser:UserClass=new UserClass()
          newUser.firstName=passedCaseUser.firstName
          newUser.lastName=passedCaseUser.lastName
          newUser.DOB=passedCaseUser.DOB
          newUser.email=passedCaseUser.email
          newUser.userId=passedCaseUser.userId
          userBase.+=(newUser.userId->newUser)
          
          reqCtx.complete("Registered New User"+ newUser.userId)
          }
      
    case getUserProfileAndInfo(reqCtx:RequestContext,byUser:String,ofUser:String)=>{
      
      var userClass=userBase.get(ofUser).orElse(userBase.get("userId0")) 
      var returnCU=new CaseUser(userBase.get(ofUser).get.userId,
          userClass.get.firstName,userBase.get(ofUser).get.lastName,
          userClass.get.DOB,userBase.get(ofUser).get.gender,
          userClass.get.email)//,userBase.get(ofUser).get.friendList,
          //userBase.get(ofUser).get.postsOfUser,userBase.get(ofUser).get.statusUpdates,
          //userBase.get(ofUser).get.photosOfUser,userBase.get(ofUser).get.statusUpdates)
          
      var returnCaseUser=returnCU.toJson
      reqCtx.complete("User Profile of "+ofUser+"Requested by "+byUser+" \n"+returnCaseUser.toString())
    }
      
      
    
    case friendRequest(reqCtx:RequestContext,byUser:String,toUser:String)=>{
      
       var userClass=userBase.get(toUser).orElse(userBase.get("userId0"))
      
      if(toUser.equalsIgnoreCase(byUser)){
        reqCtx.complete(StatusCodes.Conflict,"The user cannot send a friend request to himself")
        }
      else if(userClass.get.pendingFriendList.contains(byUser)){
        reqCtx.complete(StatusCodes.Conflict,toUser+" already has a pending friend request from "+byUser)
      }
      else{
        userClass.get.pendingFriendList+= (byUser->byUser)
        reqCtx.complete("FRIEND REQUEST \nFriend Request from "+byUser+" to "+toUser+" sent successfully\n"+
	                      "Pending friend List of "+toUser+" now is "+userClass.get.pendingFriendList+"\n")
      }
    }
    
    case acceptRejectFriendRequest(reqCtx:RequestContext,byUser:String,ofUser:String,acceptOrReject:String)=>{
      var userClass=userBase.get(ofUser).orElse(userBase.get("userId0"))   
      var pendingList=userClass.get.pendingFriendList
      
      if(userClass.get.friendList.contains(ofUser)){
          reqCtx.complete("RESPOND TO FRIEND REQUEST \n"+ofUser+" is already a friend of "+byUser)
        }
      else{
      if(pendingList.contains(ofUser)){
          if(acceptOrReject.equalsIgnoreCase("Accept")){
            userClass.get.friendList +=(pendingList.get(ofUser).get->pendingList.get(ofUser).get)
            userClass.get.pendingFriendList-=(ofUser)
            reqCtx.complete("RESPOND TO FRIEND REQUEST \n"+byUser+ " accepted the friend request of "+ofUser+"\n"+
                            "Friend List of "+byUser+" now is "+userClass.get.friendList)
          }
          else if(acceptOrReject.equalsIgnoreCase("Reject")){
            userClass.get.pendingFriendList.-=(ofUser)
            reqCtx.complete("RESPOND TO FRIEND REQUEST \n"+byUser +" rejected the friend request of "+ofUser)
          }
        }
      else{
        reqCtx.complete("RESPOND TO FRIEND REQUEST \n"+"There is no pending request from "+ofUser+" by "+byUser)
      }
      
      }
    }
      
 }
  
}

  class PageActor extends Actor{
  
  def receive={
    case createNewPage(reqCxt:RequestContext,newCasePage:CasePage,byUser:String,pageId:String)=>
       var userClass=userBase.get(byUser).orElse(userBase.get("userId0"))
    if(pages.contains(newCasePage.pageId)){
              reqCxt.complete(StatusCodes.Conflict,newCasePage.pageId+" is already used. Cannot use this PageId")
            }
          else{
          var newPage:Page=new Page()
          newPage.pageId=pageId
          newPage.pageOwner=byUser
          newPage.userList+=byUser
          
          pages.+=(newPage.pageId->newPage)
          userBase(byUser).pagesOfUser+=(newPage.pageId->newPage)
          reqCxt.complete("NEW PAGE CREATION \n"+byUser+" created new Page "+pageId)
          }
    case getAllPages(reqCtx,byUser)=>
        import spray.httpx.SprayJsonSupport._
        reqCtx.complete(pages.keys.toList)
        
    case postPostOnPage(reqCtx:RequestContext,byUser:String,pageId:String,postOnPage:CasePost)=>
      seqCounters.postSeq=seqCounters.postSeq+1
      val postSer=new Post(seqCounters.postSeq,postOnPage.post,postOnPage.timeStamp)
      pages.get(pageId).get.postList+=(seqCounters.postSeq->postSer)
      
      reqCtx.complete("POST ON PAGE\n"+byUser+" posted post on the page "+pageId+" with post id= "+seqCounters.postSeq)
      
  /*  case commentOnNodeOfPage(reqCtx:RequestContext,byUser:String,nodeType:String,pageId:String,nodeId:Int,caseComment:CaseComment)=>
      seqCounters.commentSeq=seqCounters.commentSeq+1
      var commentServer:Comment=new Comment(seqCounters.commentSeq,nodeId,caseComment.comment)
      
      if(nodeType.equalsIgnoreCase("Post")){
        
        pages.get(pageId).get.postList.get(nodeId).get.commentsOfPost+=(commentServer.commentId->commentServer)
        reqCtx.complete("Posted comment on the post "+nodeId+" on the page "+pageId)
      }
      else if(nodeType.equalsIgnoreCase("Photo")){
        pages.get(pageId).get.picList.get(nodeId).get.listOfComments+=(seqCounters.commentSeq->commentServer)
      }
    */
    case addUserToPage(reqCtx,user,pageId)=>
      pages.get(pageId).get.userList+=(user)
      reqCtx.complete("ADD USER TO PAGE\n"+user+ " was successfully added to the page "+pageId)

  }
 }
  
  class PhotoActor extends Actor{
  import spray.httpx.SprayJsonSupport._
  def receive={
    case createNewAlbumServer(reqCtx:RequestContext,ofUser:String,albumId:Int)=>
      var newAlbum=new Album()
      newAlbum.albumId=albumId
      newAlbum.albumName="Album"+albumId
      var userClass=userBase.get(ofUser).orElse(userBase.get("userId0"))
      userClass.get.albumsOfUser+=(albumId->newAlbum)
      reqCtx.complete("CREATE NEW ALBUM FOR USER/nSuccessfully created new Album "+albumId+" for the user "+ofUser)
      
  /*  case getAlbumsOfUser(requestContext:RequestContext,ofUser:String)=>
      var returnAlbumMap:Map[Int,CaseAlbum]=new HashMap[Int,CaseAlbum]
            
            for ((k,v) <- userBase.get(ofUser).get.albumsOfUser){
              var varCP=new CaseAlbum(v.albumId,v.albumName,v.photoMap.)
              returnAlbumMap+=(k->varCP)
            }
      var JsonMap=userBase.get(ofUser).get.albumsOfUser.toJson
      
      requestContext.complete(userBase.get(ofUser).get.albumsOfUser.toJson)*/
      
    case getAlbumsIdsOfUser(requestContext:RequestContext,ofUser:String)=>
    
      requestContext.complete(userBase.get(ofUser).get.albumsOfUser.keys.toList)
    
    case addPhotoToAlbum(requestContext:RequestContext,ofUser:String,albumId:Int,newPic:String)=>
      var userClass=userBase.get(ofUser).orElse(userBase.get("userId0"))
      seqCounters.picSeq=seqCounters.picSeq+1
      var newPhoto=new Photo()
      newPhoto.photoId=seqCounters.picSeq
      newPhoto.ownerId=ofUser
      newPhoto.photo=newPic
      userClass.get.albumsOfUser.get(albumId).get.photoMap+=(newPhoto.photoId->newPhoto)
      requestContext.complete("ADD PHOTO TO USER'S ALBUM\nPhoto"+newPhoto.photoId+" added successfully to the Album "+albumId)
      
    case postPhotoToUser(reqCtx:RequestContext,fromUser:String,toUser:String,newPic:String)=>
      var userClass=userBase.get(toUser).orElse(userBase.get("userId0"))
      seqCounters.picSeq=seqCounters.picSeq+1
      var newPhoto=new Photo()
      newPhoto.photoId=seqCounters.picSeq
      newPhoto.ownerId=toUser
      newPhoto.photo=newPic
      userClass.get.photosOfUser+=(newPhoto.photoId->newPhoto)
      reqCtx.complete("PHOTO POST\nPosted photo "+newPhoto.photoId+" to the user"+toUser+" by the user "+fromUser)
  }
 }
  
  
}


