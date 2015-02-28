using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;

namespace MySQLAccess
{
    public class WeiboList
    {
        public List<WeiboDetail> weibo_list=new List<WeiboDetail>();
    }
    public class WeiboDetail
    {
        public int weibo_id;
        public int user_id;
        public string user_name;
        public string content;
        public string small_pic;
        public string mid_pic;
        public string origin_pic;
        public string pub_time;
        public int like_count;
        public int comment_count;
        public int forward_count;
        public string type;

    }
    public class UserDetail
    {
        public int user_id;
        public string user_name;
    }
    public class WeiboComments
    {
        public List<Comment> weibo_comments = new List<Comment>();
    }
    public class Comment
    {
        public string user_name;
        public int user_id;
        public string comment_time;
        public string comment_text;
    }
    public class Message
    {
        public int op_type;
        public int message_id;
        public string content;
        public string created_at;
        public int from_id;
        public int to_id;
        public int weibo_id;
        public string from_name;
        public string to_name;
    }
  
}