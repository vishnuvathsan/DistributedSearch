# DistributedSearch
Code implementation for a group project on Distributed Systems module


<h3>1. Goals</h3>
<p>Develop a simple overlay-based solution that allows a set of nodes to share contents (e.g., music files) among each other. Consider a set of nodes connected via some overlay topology. Each of the nodes has a set of files that it is willing to share with other nodes. A node in the system (X) that is looking for a particular file issues a query to identify a node (Y) containing that particular file. Once the node is identified, the file can be exchanged between X and Y.</p>
<p>After completing this project, you will have developed a solution to search for contents in a distributed system. You will be able to:</p>
<ul>
<li>design, develop, and debug overlay-based applications such as simple search engines to find contents in a distributed system</li>
<li>apply RPCs or web services to develop distributed systems</li>
<li>measure an analyze the performance of a distributed system</li>
</ul>
<h3>2. Challenge</h3>
<p>The challenge consists of 4 phases. The 1<sup>st</sup> phase of the assignment is to generate the network topology and the contents of each node. The network will consist of 10+ nodes sharing 20 files among them, with each node contributing 3-5 files. Some files may be present in multiple nodes. Form the network and initialize the node contents as follows:</p>
<ol>
<li>A new node that comes up gets connected to 2 randomly selected nodes existing in the distributed system. A Bootstrap Server (BS) is provided to facilitate this step.
<ul>
<li>
<p>Following explains how to join and maintain the connectivity in the distributed system. See Section 4 for specific message formats used to talk to nodes and BS.</p>
<ol>
<li>Each new node added to the system will register at the given BS by providing node's IP address, port number, and user name.
<ul>
<li>A unique username/key is essential to keep one student's nodes separate from another.</li>
<li>BS will respond only to the messages specified in Section 4.1. Thus, it should be used only to find nodes currently in the system. It will not respond to any network formation or query messages.</li>
</ul>
</li>
<li>
<p>The 1<sup>st</sup> node will receive only an acknowledgement (ACK) from the BS. The 2<sup>nd</sup> node will receive an ACK and the details of the first node. 3<sup>rd</sup> node will receive details of the first two nodes. 4<sup>th</sup> node will receive details of first three registered nodes, and so on (all nodes are sent from BS to simplify debugging). However, from 4th node on wards you should join only to 2 randomly selected nodes from the list of nodes you received.</p>
</li>
<li>
<p>The new node joins the network via the 2 nodes learned from BS using the <em>JOIN</em> message, syntax of which is specified in Section 4.2. <em>JOIN</em> messages tell the contacted nodes that there is a new node in the system. UDP sockets are to be used to facilitate this communication.</p>
<ul>
<li>If your solution uses a routing/neighbor table, each node should either display it or should be able to do so upon request (when a command is issued).</li>
</ul>
</li>
<li>If your program crashes, you will get a 9998 from BS when you try to register a node <em>(REG)</em> again (unless you use a different IP or port). To simplify and maintain a consistent view of the distributed system at the BS (if this happens), your node needs to unregister before attempting to register again. If you are going to rerun a node with a different IP, you have to issue an unregister request <em>(UNREG)</em> for the previous entry. You may do this through Telnet (only for testing and debugging) by manually issuing the command. You may issue <em>'PRINT'</em> command through Telnet to see all the valid entries in the BS.</li>
</ol></li>
</ul>
</li>
<li>A list of file names is provided, and each node is initialized with 3 to 5 randomly selected files from this list.
<ul>
<li>
<p>Each node should either display the file names that it selected or should be able to do so upon request (when a command is issued).</p>
</li>
</ul>
</li>
</ol>
__The second phase is to design and develop a socket-based solution to find the files requested by different nodes.__ Query process is as follows:
<ol>
<li>Nodes generate requests for random file names.</li>
<li>Each request results in a query that is propagated in the network to find a node (Y) containing the file.</li>
<li>Node Y responds to the querying node with its address, which may be used to download the file.</li>
</ol>
__<p>Your solution for Phase 2 should satisfy the following requirements:</p>__
<ul>
<li>Nodes will communicate using UDP and will follow the message format given in Section 4.</li>
<li>The system should continue to operate, albeit with degraded performance, even when some nodes fail.</li>
<li>Use the given list of file names and queries to demonstrate your solution.</li>
</ul>
_No need to implement file transfer between nodes._
<p>The third phase is to extend your solution using RPCs or web services. Your modified solution in Phase 3 should satisfy the following requirements:</p>
<ul>
<li>Implementation may be based on either RPCs or web services. If you have already written web services, you are recommended to extend the solution using RPCs. Otherwise, it is recommended to use web services.</li>
<li>Update the Phase 2 design to reflect the RPCs or web services based query resolution. No need to modify the communication with the BS or neighbors while setting up the network. You may change the message format, if required.</li>
<li>Modify program in Phase 2 to reflect the RPCs or web services based implementation.</li>
</ul>
__<p>The final phase is to analyze the performance of the solutions developed in 2<sup>nd</sup> and 3<sup>rd</sup> phases.</p>__
<h3>3. Steps</h3>
<ol>
<li>Prepare a design document describing how you will implement this solution.
<ul>
<li>Report should at least include expected topology, how to communicate among nodes, format of routing table, performance parameters, how to capture them, and pseudo codes (when possible).</li>
<li>Use a layered design. Then you will be able to reuse part of this code in Phase 3.</li>
<li>Check with the lecturer before using any high-level libraries.</li>
<li>Get approval for your design from the lecturer before committing lots of time for coding.</li>
<li>Actively participate to discussions on Yammer and talk to lecturer whenever you have concerns.</li>
</ul>
</li>
<li>Develop Phase 2 of the solution.</li>
<li>Develop Phase 3 of the solution.</li>
<li>Conduct a performance analysis using solutions for both Phase 2 and Phase 3.
<ul>
<li>Ensure that at least 10 nodes are in the system.</li>
<li>You are expected to demonstrate the operation of the system as described above. In addition, prepare a report with the following results:<ol>
<li>Pick 5 nodes randomly and issue list of queries in the given file, one after the other (no parallel queries from the same node). Each query should attempt (best effort) to find at least 1 node with the given file name, if such a node exists. You should be able to search for the entire file name as well as parts of it.
<ul>
<li>e.g.: If query ask for "Lord", "Lord rings", or "Lord of the rings" consider "lord of the rings" as a match if the node has a file with that name. Consider only complete words, e.g., if you search for "Lord", file with "Lo Game" is not a match similarly if you search "Lo", "Lord of the ring" is not a match.</li>
</ul>
</li>
<li>Find number of application-level hops and latency required to resolve each query. After resolving all the queries, find the number of query messages received, forwarded, and answered by all 10+ nodes. Also find their routing table sizes and any routing related overhead/messages that may be involved (if any).</li>
<li>Remove nodes form the distributed system one at a time. Before leaving, a node must inform all the nodes in its routing table that it is leaving using <em>LEAVE</em> message. It must also tell the BS that it is leaving (using <em>UNREG</em> message). We will consider only graceful departure of nodes.</li>
<li>Repeat Steps 1 - 3, 5 times (by removing 1 node during each trial) and collect all the statistics. Every time pick a different node as the 1st node.</li>
<li>Find min, max, average, and standard deviation of hops, latency, messages per node, and node degree. Also find per query cost and per node cost. Plot distribution (CDF) of hops, latency, messages per node, and node degree.</li>
<li>Prepare a report by including your findings and critically evaluating them. Also discuss how your solution will behave, if number of queries (Q) are much larger than number of nodes (N) (Q &gt;&gt; N) and vice versa (N &gt;&gt; Q). Comment on how to improve the query resolution while reducing messages, hops, and latency.</li>
<li>Write a personal reflections statement that explains your contribution to project, other group members contributions, what you learned from the project, what you like and dislike, etc.</li>
</ol></li>
</ul>
</li>
</ol>
<h3>4. Protocol</h3>
<p>We will use a character-based protocol to make it easy to debug. You may issue commands through Telnet to the BS and other nodes to check whether commands are correctly responded. Each message starts with a <em>command</em> (in uppercase characters) that can be up to <em>n</em> characters long. Rest of the message will depend on the command. Each element in the command is separated by a <em>white space</em>.</p>
<h5>4.1 Register/Unregister With Bootstrap Server</h5>
<p><strong>Register Request message – used to register with the BS</strong></p>
<p><span style="font-family: Courier New,Courier;" face="Courier New,Courier">length REG IP_address port_no username</span></p>
<ul>
<li><i>e.g., 0036 REG 129.82.123.45 5001 1234abcd</i></li>
<li><i>length</i> – Length of the entire message including 4 characters used to indicate the length. Always give length in xxxx format to make it easy to determine the length of the message.</li>
<li><i>REG</i> – Registration request.</li>
<li><i>IP_address</i> – IP address in xxx.xxx.xxx.xxx format. This is the IP address other nodes will use to reach you. Indicated with up to 15 characters.</li>
<li><i>port_no</i> – Port number. This is the port number that other nodes will connect to. Up to 5 characters.</li>
<li><i>Username</i> – A string with characters and numbers.</li>
</ul>
<p><strong>Register Response message – BS will send the following message</strong></p>
<p><span style="font-family: Courier New,Courier;" face="Courier New,Courier">length REGOK no_nodes IP_1 port_1 IP_2 port_2</span></p>
<ul>
<li><i>e.g., 0051 REGOK 2 129.82.123.45 5001 64.12.123.190 34001</i></li>
<li><i>length</i> – Length of the entire message including 4 characters used to indicate the length. In xxxx format.</li>
<li><i>REGOK</i> – Registration response.</li>
<li><i>no_ nodes</i> – Number of node entries that are going to be returned by the registry
<ul>
<li>0 – request is successful, no nodes in the system</li>
<li>1 or 2 – request is successful, 1 or 2 nodes' contacts will be returned</li>
<li>9999 – failed, there is some error in the command</li>
<li>9998 – failed, already registered to you, unregister first</li>
<li>9997 – failed, registered to another user, try a different IP and port</li>
<li>9996 – failed, can’t register. BS full.</li>
</ul>
</li>
<li><i>IP_1</i> – IP address of the 1<sup>st</sup> node (if available).</li>
<li><i>port_1</i> – Port number of the 1<sup>st</sup> node (if available).</li>
<li><i>IP_2</i> – IP address of the 2<sup>nd</sup> node (if available).</li>
<li><i>port_2</i> – Port number of the 2<sup>nd</sup> node (if available).</li>
</ul>
<p><strong>Unregister Request message – used to unregister from the BS</strong></p>
<p><span style="font-family: Courier New,Courier;" face="Courier New,Courier">length UNREG IP_address port_no</span><span style="font-family: Courier New,Courier;" face="Courier New,Courier"> username</span></p>
<ul>
<li><i>e.g., 0028 UNREG 64.12.123.190 432</i></li>
<li><i>length</i> – Length of the entire message including 4 characters used to indicate the length. In xxxx format.</li>
<li><i>UNREG</i> – Unregister request.</li>
<li><i>IP_address</i> – IP address in xxx.xxx.xxx.xxx format. This is the IP address other nodes will use to reach you. Indicated with up to 15 characters.</li>
<li><i>port_no</i> – Port number. This is the port number that other nodes will connect to. Up to 5 characters.</li>
<li><i>username</i> – A string with characters &amp; numbers. Should be the same username used to register the node.</li>
</ul>
<p>Unregister Response message – BS will send the following message</p>
<p><span style="font-family: Courier New,Courier;" face="Courier New,Courier">length UNROK value</span></p>
<ul>
<li><i>e.g., 0012 UNROK 0</i></li>
<li><i>length</i> – Length of the entire message including 4 characters used to indicate the length. In xxxx format.</li>
<li><i>UNROK</i> – Unregister response.</li>
<li><i>value</i> – Indicate success or failure.
<ul>
<li>0 – successful</li>
<li>9999 – error while unregistering. IP and port may not be in the registry or command is incorrect.</li>
</ul>
</li>
</ul>
<p><em>For any message BS can't understand it will send an error of the format in Section 4.5.</em></p>
<h6>Testing</h6>
<p>You can use netcat to test communication with the bootstrap server. Try the following from a unix/linux terminal.<em><br /></em></p>
<p style="margin-left: 80px;"><span style="font-family: courier new,courier,monospace;">$ nc -u node1.cse.mrt.ac.lk 5000</span></p>
<p>Then issue register and unregister commands. e.g.:</p>
<p style="margin-left: 90px;"><span style="font-family: courier new,courier,monospace;">0033 REG 192.248.230.150 57000 vwb</span></p>
<h5>4.2 Join Distributed System</h5>
<p><strong>Request message – used to indicate presence of new node to other nodes that is found from BS</strong></p>
<p><span style="font-family: Courier New,Courier;" face="Courier New,Courier">length JOIN IP_address port_no</span></p>
<ul>
<li><i>e.g., 0027 JOIN 64.12.123.190 432</i></li>
<li><i>length</i> – Length of the entire message including 4 characters used to indicate the length. In xxxx format.</li>
<li><i>JOIN</i> – Join request.</li>
<li><i>IP_address</i> – IP address in xxx.xxx.xxx.xxx format. This is the IP address other nodes will use to reach you. Indicated with up to 15 characters.</li>
<li><i>port_no</i> – Port number. This is the port number that other nodes will connect to. Up to 5 characters.</li>
</ul>
<p><strong>Response message</strong></p>
<p><span style="font-family: Courier New,Courier;" face="Courier New,Courier">length JOINOK value</span></p>
<ul>
<li><i>e.g., 0014 JOINOK 0</i></li>
<li><i>length</i> – Length of the entire message including 4 characters used to indicate the length. In xxxx format.</li>
<li><i>JOINOK</i> – Join response.</li>
<li><i>value</i> – Indicate success or failure
<ul>
<li>0 – successful</li>
<li>9999 – error while adding new node to routing table</li>
</ul>
</li>
</ul>
<h5>4.3 Leave Distributed System</h5>
<p><strong>Request message – used to indicate this node is leaving the distributed system</strong></p>
<p><span style="font-family: Courier New,Courier;" face="Courier New,Courier">length LEAVE IP_address port_no</span></p>
<ul>
<li><i>e.g., 0028 LEAVE 64.12.123.190 432</i></li>
<li><i>length</i> – Length of the entire message including 4 characters used to indicate the length. In xxxx format.</li>
<li><i>LEAVE</i> – Leave request.</li>
<li><i>IP_address</i> – IP address in xxx.xxx.xxx.xxx format. This is the IP address other nodes will use to reach you. Indicated with up to 15 characters.</li>
<li><i>port_no</i> – Port number. This is the port number that other nodes will connect to. Up to 5 characters.</li>
</ul>
<p><strong>Response message</strong></p>
<p><span style="font-family: Courier New,Courier;" face="Courier New,Courier">length LEAVEOK value</span></p>
<ul>
<li><i>e.g., 0014 LEAVEOK 0</i></li>
<li><i>length</i> – Length of the entire message including 4 characters used to indicate the length. In xxxx format.</li>
<li><i>LEAVEOK</i> – Leave response.</li>
<li><i>value</i> – Indicate success or failure
<ul>
<li>If 0 – successful, if 9999 – error while adding new node to routing table</li>
</ul>
</li>
</ul>
<h5>4.4 Search for a File Name</h5>
<p><strong>Request message – Used to locate a key in the network</strong></p>
<p><span style="font-family: Courier New,Courier;" face="Courier New,Courier">length SER IP port</span><span style="font-family: Courier New,Courier;" face="Courier New,Courier"> file_name</span><span style="font-family: Courier New,Courier;" face="Courier New,Courier"> hops</span></p>
<ul>
<li>e.g., Suppose we are searching for <i>Lord of the rings</i>, <i>0047 SER 129.82.62.142 5070 "Lord of the rings"</i></li>
<li><i>length</i> – Length of the entire message including 4 characters used to indicate the length. In xxxx format.</li>
<li><i>SER</i> – Locate a file with this name.</li>
<li><i>IP</i> – IP address of the node that is searching for the file. May be useful depending your design.</li>
<li><i>port</i> – port number of the node that is searching for the file. May be useful depending your design.</li>
<li><i>file_name</i> – File name being searched.</li>
<li><i>hops</i><span style="font-style: italic;"> </span>– A hop count. May be of use for cost calculations (optional).</li>
</ul>
<p><strong>Response message – Response to query originator when a file is found.</strong></p>
<p><span style="font-family: Courier New,Courier;" face="Courier New,Courier">length SEROK no_files IP port hops filename1 filename2 ... ...</span></p>
<ul>
<li>e.g., Suppose we are searching for string <i>baby</i>. So it will return, <i>0114 SEROK 3 129.82.128.1 2301 baby_go_home.mp3 baby_come_back.mp3 baby.mpeg</i></li>
<li><i>length</i> – Length of the entire message including 4 characters used to indicate the length. In xxxx format.</li>
<li><i>SEROK</i> – Sends the result for search. The node that sends this message is the one that actually stored the (<i>key</i>, <i>value</i>) pair, i.e., node that index the file information.</li>
<li><i>no_files</i> – Number of results returned
<ul>
<li>≥ 1 – Successful</li>
<li>0 – no matching results. Searched key is not in key table</li>
<li>9999 – failure due to node unreachable</li>
<li>9998 – some other error.</li>
</ul>
</li>
<li><i>IP</i> – IP address of the node having (stored) the file.</li>
<li><i>port</i> – Port number of the node having (stored) the file.</li>
<li><i>hops</i> – Hops required to find the file(s).</li>
<li><i>filename</i> – Actual name of the file.</li>
</ul>
<h5>4.5 Error Message</h5>
<p><span style="font-family: Courier New,Courier;" face="Courier New,Courier">length ERROR</span></p>
<ul>
<li><i>0010 ERROR</i></li>
<li><i>length</i> – Length of the entire message including 4 characters used to indicate the length. In xxxx format.</li>
<li><i>ERROR</i> – Generic error message, to indicate that a given command is not understood. For storing and searching files/keys this should be send to the initiator of the message.</li>
</ul>
