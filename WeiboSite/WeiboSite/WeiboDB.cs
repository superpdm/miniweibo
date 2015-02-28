using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;
using System.Data;
using System.IO;

namespace MySQLAccess
{
    public class WeiboDB
    {

        static SQL sql = new SQL();
        public static UserDetail getUser(int user_id)
        {

               UserDetail user = new UserDetail();
                string strSQL = "select * from USER where USER_ID=" + user_id;

                DataTable dt = sql.RunSQL(strSQL);

                DataRow tr = dt.Rows[0];
                user.user_id = int.Parse(tr["USER_ID"].ToString());
                user.user_name = tr["USER_NAME"].ToString();
                return user;
            
 
        }
        public static int getCommentCount(int weibo_id)
        {
            int count = 0;
            string strSQL = "select count(*) as COUNT from COMMENT where  WEIBO_ID=" + weibo_id;
            DataTable dt=sql.RunSQL(strSQL);
            count = int.Parse(dt.Rows[0]["COUNT"].ToString());
            return count;
        }
        public static int getForwardCount(int weibo_id)
        {
            int count = 0;
            string strSQL = "select count(*) as COUNT from WEIBO where FORWARD_ID=" + weibo_id;
            DataTable dt = sql.RunSQL(strSQL);
            count = int.Parse(dt.Rows[0]["COUNT"].ToString());
            return count;
        }
        public static WeiboList getLatestWeibo(int num, bool is_raw, string type,string created_at)
        {
            string strSQL = "select * from ";
            if (is_raw)
                strSQL += " WEIBO_RAW ";
            else
                strSQL += " WEIBO ";
            strSQL += " where 1=1 ";
            if (!type.Trim().Equals(""))
                strSQL += " and TYPE='" + type.Trim() + "' ";
            if(!created_at.Trim().Equals(""))  
                strSQL += " and unix_timestamp(CREATED_AT)<unix_timestamp('" + created_at + "') ";
            strSQL += " order by CREATED_AT desc limit " + num;
            DataTable dt = sql.RunSQL(strSQL);

            WeiboList list = new WeiboList();

            for (int i = 0; i < dt.Rows.Count; i++)
            {
                WeiboDetail detail = new WeiboDetail();
                DataRow tr = dt.Rows[i];
                detail.weibo_id = int.Parse(tr["weibo_id"].ToString());
                detail.user_id = int.Parse(tr["USER_ID"].ToString());
                detail.content = tr["CONTENT"].ToString();
                detail.pub_time = tr["CREATED_AT"].ToString();
                detail.type = tr["type"].ToString();
                detail.like_count = int.Parse(tr["like_count"].ToString());


                detail.mid_pic = tr["BMIDDLE_PIC"].ToString();
                detail.small_pic = tr["THUMBNAIL_PIC"].ToString();
                detail.origin_pic = tr["ORIGINAL_PIC"].ToString();

                UserDetail user = WeiboDB.getUser(detail.user_id);
                detail.user_name = user.user_name;
                detail.comment_count = WeiboDB.getCommentCount(detail.weibo_id);
                detail.forward_count = WeiboDB.getForwardCount(detail.weibo_id);

                list.weibo_list.Add(detail);
            }
            return list;
        }

        public static WeiboComments getWeiboComments(int weibo_id)
        {
            WeiboComments comments=new WeiboComments();
            DataTable dt = sql.RunSQL("select * from COMMENT natural join USER where WEIBO_ID=" + weibo_id);
            for (int i = 0; i < dt.Rows.Count; i++)
            {
                DataRow dr = dt.Rows[i];
                Comment comment = new Comment();
                comment.user_id = int.Parse(dr["USER_ID"].ToString());
                comment.user_name = dr["USER_NAME"].ToString();
                comment.comment_text = dr["COMMENT_TEXT"].ToString();
                comment.comment_time = dr["COMMENT_TIME"].ToString();

                comments.weibo_comments.Add(comment);

            }
            return comments;
        }
        public static void pubWeibo(string created_at,string content,string source,string pic,
            double lat, double lon, int user_id, int forward_id)
        {
            string strSQL = "insert into WEIBO values(null,";
            strSQL += "'" + created_at + "',";
            strSQL += "'" + content + "',";
            strSQL += "'" + source + "',";
            strSQL += "'" + pic + "',";
            strSQL += "'" + pic + "',";
            strSQL += "'" + pic + "',";
            strSQL += " " + lat + ",";
            strSQL += " " + lon + ",";
            strSQL += " " + user_id + ",";
            strSQL += " " + forward_id + ",";
            strSQL += " " + 0 + ",";
            strSQL += "'" + "0" + "')";

            sql.RunCmd(strSQL);


        }
        public static UserDetail signIN(string email, string password)
        {
            UserDetail user = null;
            try
            {
                user = new UserDetail();
                string strSQL = "select * from USER where EMAIL='" + email + "' and PASSWORD='" + password + "'";
                DataTable dt = sql.RunSQL(strSQL);
                if (dt.Rows.Count <= 0)
                    return null;

                DataRow tr = dt.Rows[0];
                user.user_id = int.Parse(tr["USER_ID"].ToString());
                user.user_name = tr["USER_NAME"].ToString();
            }
            catch (Exception e)
            {
                return null;
            }

            return user;
        }
        public static void commentWeibo(int weibo_id, int user_id, string comment_text, string comment_time)
        {
            string strSQL = "insert into COMMENT values(";
            strSQL += weibo_id + ",";
            strSQL += user_id + ",'";
            strSQL += comment_text + "','";
            strSQL += comment_time + "',";
            strSQL += "null)";
            sql.RunCmd(strSQL);       
        }
        public static void likeWeibo(int weibo_id)
        {
            string strSQL = "update WEIBO set LIKE_COUNT=LIKE_COUNT+1 where WEIBO_ID="+weibo_id;
            sql.RunCmd(strSQL);

        }

        public static List<UserDetail> getUsers()
        {
            string strSQL = "select * from USER";
            
            DataTable dt=sql.RunSQL(strSQL);
            List<UserDetail> users = new List<UserDetail>();

            for (int i = 0; i < dt.Rows.Count; i++)
            {
                DataRow dr = dt.Rows[i];
                UserDetail user = new UserDetail();
                user.user_id = int.Parse(dr["USER_ID"].ToString());
                user.user_name=dr["USER_NAME"].ToString();
                users.Add(user);
            }

            return users;
        }
        public static WeiboList getWeiboByUser(int user_id, int num,string created_at)
        {
            string strSQL = "select * from WEIBO where user_id=" + user_id;

            if (!created_at.Trim().Equals(""))  
                strSQL += " and unix_timestamp(CREATED_AT)<unix_timestamp('" + created_at + "') ";
            strSQL += " order by CREATED_AT desc limit " + num;

            DataTable dt = sql.RunSQL(strSQL);

            WeiboList list = new WeiboList();

            for (int i = 0; i < dt.Rows.Count; i++)
            {
                WeiboDetail detail = new WeiboDetail();
                DataRow tr = dt.Rows[i];
                detail.weibo_id = int.Parse(tr["weibo_id"].ToString());
                detail.user_id = int.Parse(tr["USER_ID"].ToString());
                detail.content = tr["CONTENT"].ToString();
                detail.pub_time = tr["CREATED_AT"].ToString();
                detail.type = tr["type"].ToString();
                detail.like_count = int.Parse(tr["like_count"].ToString());


                detail.mid_pic = tr["BMIDDLE_PIC"].ToString();
                detail.small_pic = tr["THUMBNAIL_PIC"].ToString();
                detail.origin_pic = tr["ORIGINAL_PIC"].ToString();

                UserDetail user = WeiboDB.getUser(detail.user_id);
                detail.user_name = user.user_name;
                detail.comment_count = WeiboDB.getCommentCount(detail.weibo_id);
                detail.forward_count = WeiboDB.getForwardCount(detail.weibo_id);

                list.weibo_list.Add(detail);
            }
            return list;
        }
        public static void sendMessage(string content, string created_at, int from_id, int to_id, int weibo_id, int op_type)
        {
            string strSQL = "insert into MESSAGE values(null,";
            strSQL += "'" + content + "',";
            strSQL += "'" + created_at + "',";
            strSQL += " " + from_id + " ,";
            strSQL += " " + to_id + " ,";
            strSQL += " " + weibo_id + ",";
            strSQL += "" + op_type + ")";

            sql.RunCmd(strSQL);

        }
        public static WeiboDetail getWeiboByID(int weibo_id)
        {
            string strSQL = "select * from WEIBO where WEIBO_ID=" + weibo_id;

            DataTable dt=sql.RunSQL(strSQL);
            DataRow tr = dt.Rows[0];

            WeiboDetail detail = new WeiboDetail();
            detail.weibo_id = int.Parse(tr["weibo_id"].ToString());
            detail.user_id = int.Parse(tr["USER_ID"].ToString());
            detail.content = tr["CONTENT"].ToString();
            detail.pub_time = tr["CREATED_AT"].ToString();
            detail.type = tr["type"].ToString();
            detail.like_count = int.Parse(tr["like_count"].ToString());


            detail.mid_pic = tr["BMIDDLE_PIC"].ToString();
            detail.small_pic = tr["THUMBNAIL_PIC"].ToString();
            detail.origin_pic = tr["ORIGINAL_PIC"].ToString();

            UserDetail user = WeiboDB.getUser(detail.user_id);
            detail.user_name = user.user_name;
            detail.comment_count = WeiboDB.getCommentCount(detail.weibo_id);
            detail.forward_count = WeiboDB.getForwardCount(detail.weibo_id);

            return detail;
        }
        public static List<Message> getMessage(int user_id, int num)
        {
            //select content,created_at,from_id,to_id,weibo_id,a.user_name as to_name,b.user_name as from_name  
            //from MESSAGE inner join USER as a on TO_ID=a.USER_ID inner join user as b on from_id=b.user_id 
            //where TO_ID=1234 order by created_at desc limit 10
            string strSQL = "select OP_TYPE,MESSAGE_ID,CONTENT,CREATED_AT,FROM_ID,TO_ID,WEIBO_ID,a.USER_NAME as TO_NAME,b.USER_NAME as FROM_NAME";
            strSQL += " from MESSAGE inner join USER as a on TO_ID=a.USER_ID inner join USER as b on FROM_ID=b.USER_ID";
            strSQL += " where TO_ID=" + user_id + " order by CREATED_AT desc limit " + num; 
            

            DataTable dt = sql.RunSQL(strSQL);
            List<Message> msgs = new List<Message>();
            for (int i = 0; i < dt.Rows.Count; i++)
            {
                Message msg = new Message();
                DataRow dr = dt.Rows[i];
                msg.op_type = int.Parse(dr["OP_TYPE"].ToString());
                msg.message_id = int.Parse(dr["MESSAGE_ID"].ToString());
                msg.content = dr["CONTENT"].ToString();
                msg.created_at = dr["CREATED_AT"].ToString();
                msg.from_id = int.Parse(dr["FROM_ID"].ToString());
                msg.to_id = int.Parse(dr["TO_ID"].ToString());
                msg.weibo_id = int.Parse(dr["WEIBO_ID"].ToString());
                msg.from_name = dr["FROM_NAME"].ToString();
                msg.to_name = dr["TO_NAME"].ToString();
                msgs.Add(msg);
            }

            return msgs;
        }
    }

}