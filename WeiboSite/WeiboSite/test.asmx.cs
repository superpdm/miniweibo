using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;
using System.Web.Services;
using System.Data;
using System.Web.Script.Serialization;
using System.IO;
using MySQLAccess;

namespace WeiboSite
{
    /// <summary>
    /// Summary description for test
    /// </summary>
    [WebService(Namespace = "http://tempuri.org/")]
    [WebServiceBinding(ConformsTo = WsiProfiles.BasicProfile1_1)]
    [System.ComponentModel.ToolboxItem(false)]
    // To allow this Web Service to be called from script, using ASP.NET AJAX, uncomment the following line. 
    [System.Web.Script.Services.ScriptService]
    public class test : System.Web.Services.WebService
    {
        string rootDir;
        public test()
        {
            rootDir=Server.MapPath("~/");
        }
        private void log(string text)
        {
            MySQLAccess.SQL.log(text);
        }

        [WebMethod]
        public object getWeiboComments(int weibo_id)
        {
            try { 
            WeiboComments comments = WeiboDB.getWeiboComments(weibo_id);

            return JsonHelper.JsonSerializer<WeiboComments>(comments);
            }
            catch (Exception e)
            {
                log(e.Message + e.StackTrace);
                return null;
            }
        }
        [WebMethod]
        public object getLatestWeibo(int num, bool is_raw, string type,string created_at)
        {
            try
            {
                WeiboList list = WeiboDB.getLatestWeibo(num, is_raw,type,created_at);
                return JsonHelper.JsonSerializer<WeiboList>(list);
            }
            catch (Exception e)
            {
                log(e.Message + e.StackTrace);
                return null;
            }

        }
        [WebMethod]
        public void GetPicNew(string file, string fileData)
        {   
            try
            {
               
                string fileName = rootDir+"pic\\" + file;
                File.WriteAllBytes(fileName, Convert.FromBase64String(fileData));
            }
            catch (Exception e)
            {
                log(e.StackTrace+"\n"+e.Message);
            }
        }

        [WebMethod]
        public bool pubWeibo(string created_at,string content,string source,string pic,
            double lat, double lon,int user_id,int forward_id 
            )
        {
           try
           {
               created_at = getFormatedTime();
               WeiboDB.pubWeibo(created_at, content, source, pic
                   , lat, lon, user_id, forward_id);

               
           }
           catch (System.Exception ex)
           {
               log(ex.Message + ex.StackTrace);
               return false;
           }
           return true;
        }

        [WebMethod]
        public object signIN(string email, string password)
        {
            try
            {
                UserDetail user = WeiboDB.signIN(email, password);

                return JsonHelper.JsonSerializer(user);
            }
            catch (Exception e)
            {
                File.WriteAllText("C:\\log.txt", e.Message + e.StackTrace+"\n");
                log(e.Message + e.StackTrace);
                return null;
            }
        }

        [WebMethod]
        public bool commentWeibo(int weibo_id, int user_id, string comment_text, string comment_time)
        {
            try
            {
                comment_time = getFormatedTime();
                WeiboDB.commentWeibo(weibo_id, user_id, comment_text, comment_time);
            }
            catch (Exception e)
            {
                return false;
            }
            return true;
        }
        [WebMethod]
        public bool likeWeibo(int weibo_id)
        {
            try
            {
                WeiboDB.likeWeibo(weibo_id);
            }
            catch (Exception e)
            {
                return false;
            }
            return true;
        }

        [WebMethod]
        public object getUsers()
        {
            List<UserDetail> users = null;
            try
            {
                users=WeiboDB.getUsers();
            }
            catch (Exception e)
            {
                return null;
            }
            return JsonHelper.JsonSerializer(users);
        }

        [WebMethod]
        public object getWeiboByUser(int user_id, int num,string created_at)
        {
            try
            {
                WeiboList list = WeiboDB.getWeiboByUser(user_id, num,created_at);
                return JsonHelper.JsonSerializer<WeiboList>(list);
            }
            catch (Exception e)
            {
                log(e.Message + e.StackTrace);
                return null;
            }
        }
        [WebMethod]
        public bool sendMessage(string content, string created_at, int from_id, int to_id, int weibo_id,int op_type)
        {
            try
            {
                created_at = getFormatedTime();
                WeiboDB.sendMessage(content, created_at, from_id, to_id, weibo_id,op_type);
                return true;
            }
            catch (Exception e)
            {
                log(e.Message + e.StackTrace);
                return false;
            }
        }

        [WebMethod]
        public object getMessage(int user_id,int num)
        {
            List<Message> msgs = null;
            try
            {
                msgs=WeiboDB.getMessage(user_id, num);
                return JsonHelper.JsonSerializer<List<Message>>(msgs);
            }
            catch (Exception e)
            {
                log(e.Message + e.StackTrace);
                return null;
            }
        }
        [WebMethod]
        public object getWeiboByID(int weibo_id)
        {
            WeiboDetail detail = null;
            try
            {
                detail = WeiboDB.getWeiboByID(weibo_id);
                return JsonHelper.JsonSerializer(detail);
                
            }
            catch (System.Exception ex)
            {
                return null;
            }
        }

        private string getFormatedTime()
        {
            DateTime dt = DateTime.Now;
            string time=string.Format("{0:yyyy-MM-dd HH:mm:ss}", dt);
            return time;
        }
    }
}
