package nodeTypes

import scala.collection.immutable.HashMap

class Album {
  var albumId:Int=0
	var albumName:String=new String()
  var photoMap:Map[Int,Photo]=new HashMap[Int,Photo]
}