package nodeTypes

import scala.collection.mutable.ListBuffer

import scala.collection.immutable.HashMap

class Post(postIdI:Int,postI:String,timeStampI:String) {

  val postId:Int=postIdI
  val post:String=postI
  val timeStamp:String=timeStampI
  
  var commentsOfPost:Map[Int,Comment]=new HashMap[Int,Comment]
}