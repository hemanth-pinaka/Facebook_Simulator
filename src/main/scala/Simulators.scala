import akka.actor.{ActorSystem,Actor,Props,ActorRef}
import akka.pattern.ask
import scala.concurrent._
import scala.collection.mutable.ArrayBuffer
import scala.collection.mutable.HashMap
import scala.collection.mutable.LinkedList
import ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration;
import java.util.concurrent.TimeUnit;
import scala.util.Random
import akka.util.Timeout
import FacebookCommon._



object Constants{
      
  var genderList:List[String]=List("Male","Female")
  var acceptReject:List[String]=List("Accept","Accept","Accept","Accept","Reject")

}

object Simulators {
  def main(arg:Array[String]){
  
  implicit val timeout=Timeout(Duration.create(5000, TimeUnit.MILLISECONDS))
   
  
  var noOfUsers=arg(0).toInt
  
  val friendRequestSchedulerDelay=5000
  val statusUpdateSchedulerDelay=500
  val messageSchedulerDelay=1000
  val poolUserProfileSchedulerDelay=4000
  val createNewPageShedulerDelay=4000
  val joinUserToPageSchedulerDelay=5000
  val commentOnPostOfUserDelay=5000
  val PostOnPagesSchedulerDelay=5000
  val CreateNewPicAlbumSchedulerDelay=2000
  val AddPhotoToAlbumOfUserDelay=1000
  val PostPicToUserSchedulerDelay=1000
  
  var pageIndex=0
   
  
  
  implicit val userSystem = ActorSystem()
  var startTime = System.currentTimeMillis()
  var actorMap:HashMap[String,ActorRef]=new HashMap[String,ActorRef]
  for(i<- 0 to noOfUsers-1){
    println(i)
    
    var testActor=userSystem.actorOf(Props[FaceBookClientClasses.User],"simulationActor"+i)
    
    val newCaseUser:CaseUser=new CaseUser("userId"+i,"firstName"+i,"lastName"+i,"DOB"+i,Constants.genderList(Random.nextInt(Constants.genderList.length)),"email"+i)
    
   testActor!registerNewUserClient(newCaseUser)
   actorMap.+=("userId"+i->testActor)
   // Thread.sleep(15)
    
  }
  
  Thread.sleep(noOfUsers*3)
  var time = System.currentTimeMillis() - startTime
  println("Time = "+time)
  Thread.sleep(5000)
  println("----------------------------------------------------------------------------------------------------------------------")
  println("----------------------------------USER ACTIVITY STARTED---------------------------------------------------------------")
  
  def friendRequestScheduler()={
    var randomVariables=getRandomUsers()
    var sender=randomVariables._2._1
    var receiverIndex=randomVariables._1._2
    var senderIndex=randomVariables._1._1
    
    
    sender!sendFriendRequest(receiverIndex)
    
    Thread.sleep(10)
    
    var acceptOrReject:String=Constants.acceptReject(Random.nextInt(Constants.acceptReject.length))
    actorMap(receiverIndex)!respondToFriendRequest(senderIndex,acceptOrReject)
    
  }
  
  def statusUpdateScheduler()={       //Status Update Scheduler block 
    var sender=getRandomUsers()._2._1
    
    sender!updateStatusClient("statusUpdate"+System.currentTimeMillis(),"timeStamp")
  }
  
  
  def messageScheduler()={                        //Message Scheduler block
    var randomVariables=getRandomUsers() 
    var sender=randomVariables._2._1
    var receiver=randomVariables._2._2
    var receiverIndex=randomVariables._1._2
    var senderIndex=randomVariables._1._1
    
    println("receiver= "+receiverIndex)
    sender!sendPostToUser(receiverIndex,"message to userId"+receiverIndex+" from "+senderIndex+" at "+System.currentTimeMillis(),"timeStamp")
    }
  

  
  
  def poolUserProfileScheduler()={
    var randomVariables=getRandomUsers() 
    var sender=randomVariables._2._1
    var receiver=randomVariables._2._2
    var receiverIndex=randomVariables._1._2
    var senderIndex=randomVariables._1._1
    
    sender!getUserProfileAndInfoClient(receiverIndex)
  }
  
  def commentOnPostOfUserScheduler()={    
    var randomVariables=getRandomUsers() 
    var sender=randomVariables._2._1
    var receiver=randomVariables._2._2
    var receiverIndex=randomVariables._1._2
    var senderIndex=randomVariables._1._1
    
    sender!getPostsOfUserAndCommentClient(receiverIndex)
  }
  
  def createNewPageSheduler()={
    pageIndex=pageIndex+1
    var randomUser=getRandomUsers
    var newPageName="page"+pageIndex
    var casePage=new CasePage(newPageName,randomUser._1._1)
    
    randomUser._2._1!createNewPageClient(newPageName,casePage)
  }
  
  def joinUserToPageScheduler()={
    var randomUser=getRandomUsers
    var randomUserName=randomUser._1._1
    var randomUserActor=randomUser._2._1
    
    randomUserActor!getPagesAndJoinUserToPage(randomUserName)
  }
  
  def commentOnPostOfUser()={
    var randomUsers=getRandomUsers
    var randomCommentorIndex=randomUsers._1._1
    var randomUserCommentor=randomUsers._2._1
    
    var randomReceiverIndex=randomUsers._1._2
    var randomUserReceiver=randomUsers._2._2
    
    randomUserCommentor!getPostsOfUserAndCommentClient(randomReceiverIndex)
  }
  
  def PostOnPagesScheduler()={
    var randomUsers=getRandomUsers
    var randomPosterIndex=randomUsers._1._1
    var randomPoster=randomUsers._2._1
    
    
    randomPoster!getPagesAndPostOnPageClient()
  }
  
  def CreateNewPicAlbumScheduler()={
    var randomUsers=getRandomUsers
    var randomUserIndex=randomUsers._1._1
    var randomUser=randomUsers._2._1
    
    randomUser!createNewAlbumClient(randomUserIndex)
  }
  
  def CheckAlbumsOfUser()={
    var randomUsers=getRandomUsers
    var randomUserIndex=randomUsers._1._1
    var randomUser=randomUsers._2._1
    
    randomUser!getAlbumsOfUserClient(randomUserIndex)
  }

  
   def AddPhotoToAlbumOfUser()={
    var randomUsers=getRandomUsers
    var randomUserIndex=randomUsers._1._1
    var randomUser=randomUsers._2._1
    
    randomUser!getAlbumsAndAddPhotoToAlbumClient()
  }
   
   def PostPicToUserScheduler()={
    var randomUsers=getRandomUsers
    var randomSenderIndex=randomUsers._1._1
    var randomSender=randomUsers._2._1
    var randomReceiverIndex=randomUsers._1._2
    var randomReceiver=randomUsers._2._2
    
    println("Random sedner= "+randomSenderIndex+" and Random receiver= "+randomReceiverIndex)
    
    randomSender!postPicToUserClient(randomReceiverIndex)
  }
  
  
    
        userSystem.scheduler.schedule(
        Duration.create(5000, TimeUnit.MILLISECONDS), Duration.create(friendRequestSchedulerDelay, TimeUnit.MILLISECONDS))(friendRequestScheduler)  //get User Profile and Info Scheduler block 
      
        userSystem.scheduler.schedule(
        Duration.create(5000, TimeUnit.MILLISECONDS), Duration.create(statusUpdateSchedulerDelay, TimeUnit.MILLISECONDS))(statusUpdateScheduler)  //get User Profile and Info Scheduler block 
      
        userSystem.scheduler.schedule(
        Duration.create(600, TimeUnit.MILLISECONDS), Duration.create(messageSchedulerDelay, TimeUnit.MILLISECONDS))(messageScheduler)   
       
        userSystem.scheduler.schedule(
        Duration.create(5000, TimeUnit.MILLISECONDS), Duration.create(poolUserProfileSchedulerDelay, TimeUnit.MILLISECONDS))(poolUserProfileScheduler)  //get User Profile and Info Scheduler block
        
        userSystem.scheduler.schedule(
        Duration.create(0, TimeUnit.MILLISECONDS), Duration.create(createNewPageShedulerDelay, TimeUnit.MILLISECONDS))(createNewPageSheduler)  //create new page Scheduler block
 
        userSystem.scheduler.schedule(
        Duration.create(1000, TimeUnit.MILLISECONDS), Duration.create(joinUserToPageSchedulerDelay, TimeUnit.MILLISECONDS))(joinUserToPageScheduler)  //join user to page Scheduler block

        userSystem.scheduler.schedule(
        Duration.create(1000, TimeUnit.MILLISECONDS), Duration.create(commentOnPostOfUserDelay, TimeUnit.MILLISECONDS))(commentOnPostOfUser)
        
        userSystem.scheduler.schedule(
        Duration.create(1000, TimeUnit.MILLISECONDS), Duration.create(PostOnPagesSchedulerDelay, TimeUnit.MILLISECONDS))(PostOnPagesScheduler)

        userSystem.scheduler.schedule(
        Duration.create(3000, TimeUnit.MILLISECONDS), Duration.create(CreateNewPicAlbumSchedulerDelay, TimeUnit.MILLISECONDS))(CreateNewPicAlbumScheduler)
        
        userSystem.scheduler.schedule(
        Duration.create(3000, TimeUnit.MILLISECONDS), Duration.create(AddPhotoToAlbumOfUserDelay, TimeUnit.MILLISECONDS))(AddPhotoToAlbumOfUser)
        
        userSystem.scheduler.schedule(
        Duration.create(3000, TimeUnit.MILLISECONDS), Duration.create(1000, TimeUnit.MILLISECONDS))(PostPicToUserScheduler)
        
        
        
def getRandomUsers():((String,String),(ActorRef,ActorRef))={
  var randomActorSenderIndex=actorMap.keys.toList(Random.nextInt(actorMap.size))
  var requestSender:ActorRef=actorMap(randomActorSenderIndex)
 
  
  var randomActorReceiverIndex=actorMap.keys.toList(Random.nextInt(actorMap.size))
  
  var requestReceiver:ActorRef=actorMap(randomActorReceiverIndex)
 
  
  return ((randomActorSenderIndex,randomActorReceiverIndex),(requestSender,requestReceiver))
 
}
    

  
 }
    
}

