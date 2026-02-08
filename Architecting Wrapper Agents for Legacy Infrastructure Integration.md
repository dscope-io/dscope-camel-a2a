To integrate fully automated agents into complex existing infrastructures like Supply Chain (RosettaNet) or Finance (Swift/ISO20022) using the Agent-to-Agent (A2A) protocol, you can adopt a **"Wrapper Agent" architecture**. In this model, A2A serves as the high-level "negotiation and intent" layer, while established integration frameworks (like Apache Camel or Spring Boot) handle the low-level "execution and compliance" layer.  
Based on your sources, here is a detailed breakdown of how to architect this solution:

### 1\. The Core Architecture: Separation of Intent vs. Execution

The A2A protocol is designed for **peer-to-peer collaboration** and high-level task management, not for transmitting raw binary streams or legacy XML formats directly 1\.

* **A2A Layer (The "Boardroom"):** Agents use A2A to find each other, negotiate terms, and agree on a task (e.g., "Order 500 units" or "Settle Invoice \#99").  
* **Infrastructure Layer (The "Engine Room"):** Once an agreement is reached via A2A, the agent triggers internal tools (often via the Model Context Protocol, MCP) or services to execute the transaction using the specific industry standard (RosettaNet PIPs or ISO20022 XML) 3\.

### 2\. Implementation Strategy: The "Wrapper Agent"

You can use Java-based implementations (like a2ajava or Spring AI A2A) to wrap your existing legacy infrastructure into an "Intelligent Agent" 5\.

#### Step A: Define the Agent Card (Discovery)

The first step is to create an **Agent Card** for your legacy system. This JSON file (hosted at /.well-known/agent.json) abstracts the complexity of the underlying protocol 7\.

* **Example (Supply Chain):** Instead of exposing a raw "RosettaNet PIP 3A4" endpoint, your Agent Card advertises a skill named "manage\_purchase\_order" with simple inputs like product\_id, quantity, and delivery\_date.  
* **Example (Finance):** Instead of exposing "ISO20022 pacs.008", the Agent Card advertises "execute\_cross\_border\_payment" taking amount, currency, and beneficiary as inputs.

#### Step B: The Bridge (Spring Boot & Apache Camel)

The sources highlight utilizing **Spring Boot** and **Apache Camel** to bridge the gap between the AI agent and the legacy infrastructure 9\.

* **Spring Boot**: Acts as the A2A Server. You annotate a Java method with @Action (e.g., processOrder) which the A2A protocol exposes to other agents 11\.  
* **Apache Camel**: Inside that @Action method, you trigger a Camel Route. Camel is ideal here because it has mature patterns for handling asynchronous messaging, file transfers, and XML transformations required by Swift and RosettaNet 10\.

**Hypothetical Workflow for Supply Chain (RosettaNet):**

* **A2A Interaction:** A "Buyer Agent" sends an A2A task to your "Supplier Agent": *“Can you fulfill order \#123?”*.  
* **Internal Processing:** The Supplier Agent receives the task. It checks inventory (via MCP/SQL).  
* **Execution:** Upon confirmation, the Supplier Agent invokes a Camel route.  
* *Camel Route:* from("direct:confirmOrder").to("freemarker:rosettanet-pip3a4.ftl").to("http://legacy-b2b-gateway").  
* This converts the intent into a valid RosettaNet XML document and pushes it to the traditional B2B gateway.

**Hypothetical Workflow for Finance (Swift/ISO20022):**

* **A2A Interaction:** A "Procurement Agent" requests payment from the "Treasury Agent".  
* **Validation:** The Treasury Agent validates the request against policy (e.g., "Is this amount under the auto-approval limit?").  
* **Execution:** The Treasury Agent triggers a secure payment tool.  
* *Tool:* Generates an ISO20022 pacs.008 XML file.  
* *Transport:* Uses a secure file transfer (SFTP or MQ) to send the file to the Swift gateway.  
* **Completion:** The agent returns an A2A artifact (e.g., a payment tracking ID) to the requestor 12\.

### 3\. Key Integration Technologies

The sources identify specific technologies to facilitate this:

* **A2A \+ MCP Synergy:** Use A2A for the agent-to-agent negotiation and MCP (Model Context Protocol) to connect the agent to the actual "dumb" pipes of the infrastructure. For example, a "Bank Agent" uses A2A to talk to a client, but uses MCP to talk to the Swift database 3\.  
* **Java Annotations:** Frameworks like a2ajava allow you to convert a legacy Spring service into an agent simply by adding @Agent and @Action annotations, making it "AI-ready" without rewriting the core logic 13\.  
* **Asynchronous Handling:** Both Supply Chain and Finance are often asynchronous (you don't get a confirmation instantly). A2A supports **Long-running Tasks** and **Push Notifications** (Webhooks). The agent can submit the RosettaNet request and then "sleep" or do other work, notifying the buyer only when the B2B gateway returns an acknowledgment hours later 12\.

### 4\. Security & Governance (Crucial for Finance/Supply Chain)

Connecting AI to Swift or Supply Chain grids requires strict security. The A2A protocol supports:

* **Identity:** Agents utilize Agent Cards and standard OIDC/OAuth2 for identity. You can restrict discovery to a "Curated Registry" (private directory) so only authorized internal agents can find the "Treasury Agent" 15\.  
* **Auditability:** Every A2A interaction is a "Task" with a unique ID and history. This provides the audit trail required for financial compliance (e.g., "Why did the agent approve this transfer? Because Agent B asked for it at 10:00 AM") 17\.  
* **Human-in-the-loop:** For high-value transactions (e.g., Swift transfers \> $10k), the A2A agent can be configured to pause and request human approval before triggering the final execution step 18\.

### Summary Table: From Legacy to Agentic

Feature,Traditional Infrastructure (RosettaNet/Swift),Agentic Layer (A2A Protocol),Integration Bridge  
Discovery,"Static IPs, DNS, Service Registries",Agent Card (/.well-known/agent.json),Agent Card maps intent to legacy endpoints.  
Protocol,"XML, EDI, binary, SOAP",JSON-RPC 2.0 over HTTP/WebSocket,A2A server translates JSON intent to XML/EDI.  
Workflow,"Rigid, pre-defined process (PIPs)","Dynamic, negotiated workflow",Agent logic determines which PIP to trigger.  
Security,"VPN, Mutual TLS, Firewall","OAuth2, OIDC, Token-based",Service accounts allow Agents to authenticate.  
By wrapping your RosettaNet and Swift gateways in A2A-compliant shells, you transform rigid infrastructure into flexible, conversational services that other AI agents can discover and utilize autonomously.  
