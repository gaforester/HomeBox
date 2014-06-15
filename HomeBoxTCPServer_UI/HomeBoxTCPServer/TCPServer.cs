using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace HomeBoxTCPServer
{
    class TCPServer
    {
        private int _port;

        public int Port
        {
            get { return _port; }
            set { _port = value; }
        }
    }
}
