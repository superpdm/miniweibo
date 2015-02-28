using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Data;
using System.Web;
using MySql.Data.MySqlClient;
using System.IO;
using System.Configuration;


namespace MySQLAccess
{
    public class SQL
    {
        

        private MySqlConnection _connection;
        public string connectionString;
        
        public SQL()
        {
            connectionString = ConfigurationManager.AppSettings["connectionString"]; 
            MySqlConnection conn = new MySqlConnection(connectionString);
            _connection = conn;
            this.open();

        }
        private static string getFormatedTime()
        {
            DateTime dt = DateTime.Now;
            string time = string.Format("{0:yyyy-MM-dd HH:mm:ss}", dt);
            return time;
        }
        public static void log(string txt)
        {
            string path = System.AppDomain.CurrentDomain.BaseDirectory;
            path += "log.txt";

            File.AppendAllText(path,getFormatedTime()+"\t\t"+txt+"\n");
        }
        #region Connection

        public void open()
        {
            if (this._connection.State != ConnectionState.Open)
                this._connection.Open();

        }

        public void close()
        {
            if (this._connection.State == ConnectionState.Open)
                this._connection.Close();
        }
        #endregion



        #region SQL
        public DataTable RunSQL(String strSQL)
        {
            log(strSQL);
            open();
            MySqlDataAdapter sqlDa = new MySqlDataAdapter(strSQL, _connection);

            DataSet ds = new DataSet();
            sqlDa.Fill(ds);
            
            this.close();
            return ds.Tables[0];
        }

        /// <summary>
        /// 函数功能：执行数据更新的SQL命令语句：INSERT,UPDATE,DELETE
        /// </summary>
        /// <param name="strSQL"></param>
        public void RunCmd(String strSQL)
        {
            log(strSQL);
            open();
            //con.BeginTransaction();
            MySqlCommand cmd = new MySqlCommand(strSQL, _connection);
            cmd.CommandType = CommandType.Text;
            cmd.ExecuteNonQuery();
            
            close();
        }


        /// <summary>
        ///  函数功能：执行SQL语句命令strSQL，返回结果集中第一行的第一列
        /// </summary>
        /// <param name="strSQL"></param>
        /// <returns></returns>
        public int RunCmdScalar(String strSQL)
        {
            open();
            MySqlCommand cmd = new MySqlCommand();
            cmd.CommandType = CommandType.Text;
            cmd.CommandText = strSQL;
            cmd.Connection = _connection;
            int pp = (int)cmd.ExecuteScalar();
            close();
            return pp;
        }
        #endregion

    }
}
