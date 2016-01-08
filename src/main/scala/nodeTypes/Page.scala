package nodeTypes

import scala.collection.mutable.MutableList
import scala.collection.mutable.HashMap

class Page {
  var pageId=new String()
  var pageOwner=new String()
  var userList=new MutableList[String]()
  var commentList=new HashMap[Int,Comment]()
  var picList=new HashMap[Int,Photo]()
  var likeList=new MutableList[String]()
  var postList=new HashMap[Int,Post]()
  
}