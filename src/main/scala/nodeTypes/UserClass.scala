package nodeTypes

import scala.collection.immutable.HashMap
import scala.collection.immutable.List
import scala.collection.mutable.ListBuffer

class UserClass{
  var userId=new String()
  var firstName:String=new String()
  var lastName:String=new String()
  var DOB:String=new String()
  var gender:String=new String()
  var email:String=new String()
  var pendingFriendList:Map[String,String]=new HashMap[String,String]
  var friendList:Map[String,String]=new HashMap[String,String]
  var postsOfUser:HashMap[Int,Post]=new HashMap[Int,Post]()
  var statusUpdates:HashMap[Int,Post]=new HashMap[Int,Post]()
  var photosOfUser:Map[Int,Photo]=new HashMap[Int,Photo]()
  var albumsOfUser:Map[Int,Album]=new HashMap[Int,Album]()
  var pagesOfUser:Map[String,Page]=new HashMap[String,Page]()
  
  
}