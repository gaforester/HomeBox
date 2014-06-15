//TCP Set 2 Project by Roger Bryant and Deuane Hessel

using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows.Forms;
using System.Net;
using System.Net.Sockets;
using System.IO;
using System.Data.OleDb;
using System.Diagnostics;

namespace HomeBoxTCPServer
{
    public partial class Form1 : Form
    {
        public Form1()
        {
            InitializeComponent();
            updateFileList();
        }

        private void btnServer_Click(object sender, EventArgs e)
        {
            string jarPath = @"C:\HomeBox\server.jar";
            Process server = new Process();
            server.StartInfo.FileName = @"C:\Program Files\Java\jre7\bin\java.exe";
            server.StartInfo.Arguments = "-jar " + jarPath;
            server.StartInfo.CreateNoWindow = true;
            server.StartInfo.WindowStyle = ProcessWindowStyle.Hidden;
            server.StartInfo.UseShellExecute = false;
            server.StartInfo.WorkingDirectory = @"C:\HomeBox";
            int pId = -1;

            if (btnServer.Text == "Run Server")
            {
                int portNum = int.Parse(txtPort.Text);
                Watch();
                btnServer.Text = "Stop Server";
                msgStatus.Text = "Listening";
                msgStatus.ForeColor = Color.Green;
                try
                {
                    server.Start();
                    pId = server.Id;
                }
                catch (Exception ex)
                {
                    string exMsg = "Server >> Server is already running." + Environment.NewLine;
                    File.AppendAllText(@"C:\HomeBox\log.txt", exMsg);
                }
            }
            else
            {
                try
                {
                    //server = Process.GetProcessById(pId);
                    server.Close();
                    string closeMsg = "Server >> Connection Closed." + Environment.NewLine;
                    File.AppendAllText(@"C:\HomeBox\log.txt", closeMsg);
                }
                catch (Exception ex)
                { 
                    string exMsg = "Server >> Server not running and could not be closed." + Environment.NewLine;
                    File.AppendAllText(@"C:\HomeBox\log.txt", exMsg); 
                }
                btnServer.Text = "Run Server";
                msgStatus.Text = "Stopped";
                msgStatus.ForeColor = Color.Red;
            }
        }

        private void btnClearLog_Click(object sender, EventArgs e)
        {
            txtLog.Text = "";
        }

        private void btnPort_Click(object sender, EventArgs e)
        {
            if (btnPort.Text == "Change Port" && msgStatus.Text != "Listening")
            {
                txtPort.ReadOnly = false;
                btnPort.Text = "Save Port";
            }
            else if (btnPort.Text == "Save Port" && msgStatus.Text != "Listening")
            {
                string portNum = txtPort.Text;
                System.IO.File.WriteAllText(@"C:\HomeBox\port.txt", portNum);
                txtPort.ReadOnly = true;
                btnPort.Text = "Change Port";
            }
        }

        private void btnOpen_Click(object sender, EventArgs e)
        {
            Process.Start("explorer.exe", @"c:\HomeBox");
        }

        private void populateFileList(ListBox listBox, string folder, string fileType)
        {
            DirectoryInfo info = new DirectoryInfo(folder);
            FileInfo[] files = info.GetFiles(fileType);
            foreach (FileInfo file in files)
            {
                listBox.Items.Add(file.Name);
            }
        }

        private void btnRefresh_Click(object sender, EventArgs e)
        {
            updateFileList();
        }

        private void updateFileList()
        {
            fileList.Items.Clear();
            populateFileList(fileList, @"C:\HomeBox", "*.jpg");
        }

        public void Watch() 
        {
            var watch = new FileSystemWatcher();
            watch.Path = @"C:\HomeBox";
            watch.Filter = "log.txt";
            watch.NotifyFilter = NotifyFilters.LastWrite;
            watch.Changed += new FileSystemEventHandler(OnChanged);
            watch.EnableRaisingEvents = true;
        }

        private void OnChanged(object source, FileSystemEventArgs e)
        {
            try
            {
                string txt = File.ReadAllText(@"C:\HomeBox\log.txt");
                SetText(txt.ToString());
            }
            catch (Exception ex) { }
        }

        delegate void SetTextCallback(string text);

        private void SetText(string text)
        {
            if (this.txtLog.InvokeRequired)
            {
                SetTextCallback d = new SetTextCallback(SetText);
                this.Invoke(d, new object[] { text });
            }
            else
            {
                this.txtLog.Text = text + Environment.NewLine;
            }
        }

        private void btnOpenLog_Click(object sender, EventArgs e)
        {
            Process.Start(@"C:\HomeBox\log.txt");
        }
    }
}
