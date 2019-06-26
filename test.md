第1章    标准化词汇表

第2章    Kubernetes问题与安全

2.1    Kubernetes问题跟踪器

2.2    Kubernetes安全和披露信息

第3章    使用Kubernetes API

3.1    Kubernetes API概述

REST API是Kubernetes的基本结构。组件和外部用户命令之间的所有操作和通信都是API服务器处理的REST API调用。因此，Kubernetes平台中的所有内容都被视为API对象，并在API中具有相应的条目 。

大多数操作可以通过 kubectl命令行界面或其他命令行工具执行，例如kubeadm，后者又使用API。但是，您也可以使用REST调用直接访问API。

如果使用Kubernetes API编写应用程序，请考虑使用其中一个客户端库。

API**版本控制**

为了消除字段或重构资源表示，Kubernetes支持多个API版本，每个版本位于不同的API路径。例如：/api/v1或 /apis/extensions/v1beta1。

版本设置为API级别，而不是资源或字段级别：

   确保API提供清晰一致的系统资源和行为视图。

   启用对寿命终止和/或实验API的控制访问。

JSON和Protobuf序列化模式遵循相同的模式更改指南。以下描述涵盖两种格式。

注意： API版本控制和软件版本控制是间接相关的。该API和发行版本的提案描述API版本和软件版本之间的关系

不同的API版本表示不同级别的稳定性和支持。您可以在API更改文档中找到有关每个级别的条件的更多信息。

以下是每个级别的摘要：

Alpha:

   版本名称包含alpha（例如v1alpha1）。

   该软件可能包含错误。启用功能可能会暴露错误。默认情况下可以禁用某项功能。

   可以随时删除对功能的支持，恕不另行通知。

   API可能会在以后的软件版本中以不兼容的方式更改，恕不另行通知。

   由于错误风险增加和缺乏长期支持，建议仅在短期测试集群中使用该软件。

Beta：

   版本名称包含beta（例如v2beta3）。

   该软件经过了充分测试。启用某项功能被认为是安全的。默认情况下启用功能。

   虽然细节可能会发生变化，但不会删除对功能的支持。

   在随后的beta版或稳定版中，对象的模式和/或语义可能以不兼容的方式发生变化。发生这种情况时，会提供迁移说明。这可能需要删除，编辑和重新创建API对象。编辑过程可能需要一些思考。对于依赖该功能的应用程序，这可能需要停机时间。

   建议该软件仅用于非关键业务用途，因为后续版本可能会发生不兼容的更改。如果您有多个可以独立升级的群集，您可以放宽此限制。

注意：尝试测试版功能并提供反馈。功能退出测试版后，进行更多更改可能不切实际。

Stable:

该版本名称是vX这里X是一个整数。

稳定版本的功能出现在许多后续版本的已发布软件中。

API groups

API组可以更轻松地扩展Kubernetes API。API组在REST路径和apiVersion序列化对象的字段中指定。

目前，有几个API组正在使用中：

所述芯（也称为遗留）基团，其在REST路径/api/v1和没有被指定为所述的部分apiVersion字段，例如，apiVersion: v1。

命名组处于REST路径/apis/$GROUP_NAME/$VERSION，并使用apiVersion: $GROUP_NAME/$VERSION （例如apiVersion: batch/v1）。您可以在Kubernetes API参考中找到支持的API组的完整列表。

支持使用自定义资源扩展API的两个路径是：

CustomResourceDefinition 用于满足基本的CRUD需求。

聚合器，用于实现自己的apiserver的全套Kubernetes API语义。

启用API**组**

默认情况下启用某些资源和API组。您可以通过设置--runtime-config apiserver 来启用或禁用它们。--runtime-config接受逗号分隔值。例如： - 要禁用batch / v1，set --runtime-config=batch/v1=false - 启用batch / v2alpha1，set --runtime-config=batch/v2alpha1 该标志接受逗号分隔的一组key = value对，用于描述apiserver的运行时配置。

注意：启用或禁用组或资源时，需要重新启动apiserver和controller-manager以获取--runtime-config更改。

启用组中的资源

默认情况下启用DaemonSets，Deployments，HorizontalPodAutoscalers，Ingress，Jobs和ReplicaSet。您可以通过设置--runtime-configapiserver 来启用其他扩展程序资源。--runtime-config接受逗号分隔值。例如，要禁用部署和作业，请设置 --runtime-config=extensions/v1beta1/deployments=false,extensions/v1beta1/jobs=false

 

 

3.2    Kubernetes API概念

Kubernetes API是通过HTTP提供的基于资源（RESTful）的编程接口。它支持通过标准HTTP谓词（POST，PUT，PATCH，DELETE，GET）检索，创建，更新和删除主要资源，包括允许细粒度授权的许多对象的其他子资源（例如将pod绑定到节点）为了方便或有效，可以接受和服务于不同表示形式的资源。它还通过“监视”和一致列表支持对资源的有效更改通知，以允许其他组件有效地缓存和同步资源状态。

标准API**术语**

大多数Kubernetes API资源类型都是“对象” - 它们代表集群上概念的具体实例，如pod或命名空间。API资源类型的一个较小数目是“虚拟” -它们通常代表操作，而不是物体，如权限检查（使用具有的JSON编码主体中的POST SubjectAccessReview到subjectaccessreviews资源）。所有对象都具有唯一的名称以允许幂等创建和检索，但如果虚拟资源类型不可检索或不依赖于幂等性，则它们可能没有唯一的名称。

Kubernetes通常利用标准的RESTful术语来描述API概念：

一个资源类型是在URL中使用的名称（pods，namespaces，services）

所有资源类型都在JSON（它们的对象模式）中具有具体表示，称为种类

资源类型的实例列表称为集合

资源类型的单个实例称为资源

所有资源类型都由cluster（/apis/GROUP/VERSION/）作用域或命名空间（/apis/GROUP/VERSION/namespaces/NAMESPACE/）。删除其名称空间时，将删除名称空间作用域资源类型，并通过对名称空间作用域的授权检查来控制对该资源类型的访问。以下路径用于检索集合和资源：

集群范围的资源：

GET /apis/GROUP/VERSION/RESOURCETYPE - 返回资源类型的资源集合

GET /apis/GROUP/VERSION/RESOURCETYPE/NAME - 在资源类型下使用NAME返回资源

命名空间范围的资源：

GET /apis/GROUP/VERSION/RESOURCETYPE - 跨所有命名空间返回所有资源类型实例的集合

GET /apis/GROUP/VERSION/namespaces/NAMESPACE/RESOURCETYPE - 返回NAMESPACE中资源类型的所有实例的集合

GET /apis/GROUP/VERSION/namespaces/NAMESPACE/RESOURCETYPE/NAME - 在NAMESPACE中使用NAME返回资源类型的实例

由于命名空间是一个集群范围的资源类型，因此您可以检索所有命名空间的列表GET /api/v1/namespaces以及有关特定命名空间的详细信息GET /api/v1/namespaces/NAME。

 

几乎所有对象资源类型都支持标准HTTP谓词 - GET，POST，PUT，PATCH和DELETE。Kubernetes使用术语列表来描述返回一组资源，以区别于检索通常称为get的单个资源。

 

某些资源类型将具有一个或多个子资源，表示为资源下方的子路径：

 

集群范围的子资源： GET /apis/GROUP/VERSION/RESOURCETYPE/NAME/SUBRESOURCE

命名空间范围的子资源： GET /apis/GROUP/VERSION/namespaces/NAMESPACE/RESOURCETYPE/NAME/SUBRESOURCE

每个子资源支持的谓词将根据对象而有所不同 - 请参阅API文档更多信息。不可能跨多个资源访问子资源 - 通常，如果有必要，将使用新的虚拟资源类型。

 

有效检测变化

要使客户端能够构建集群当前状态的模型，需要所有Kubernetes对象资源类型来支持一致性列表和称为监视的增量更改通知源。每个Kubernetes对象都有一个resourceVersion字段，表示存储在底层数据库中的该资源的版本。检索资源集合（命名空间或集群作用域）时，服务器的响应将包含一个resourceVersion值，该值可用于启动对服务器的监视。服务器将返回提供后发生的所有更改（创建，删除和更新）resourceVersion。这允许客户端获取当前状态，然后在不丢失任何更新的情况下监视更改。如果客户端监视断开连接，则可以从上次返回的表重新启动新监视resourceVersion，或执行新的收集请求并重新开始。

 

例如：

 

列出给定命名空间中的所有pod。

 

GET /api/v1/namespaces/test/pods

---

200 OK

Content-Type: application/json

{

  "kind": "PodList",

  "apiVersion": "v1",

  "metadata": {"resourceVersion":"10245"},

  "items": [...]

}

从资源版本10245开始，接收任何创建，删除或更新的通知作为单独的JSON对象。

 

GET /api/v1/namespaces/test/pods?watch=1&resourceVersion=10245

---

200 OK

Transfer-Encoding: chunked

Content-Type: application/json

{

  "type": "ADDED",

  "object": {"kind": "Pod", "apiVersion": "v1", "metadata": {"resourceVersion": "10596", ...}, ...}

}

{

  "type": "MODIFIED",

  "object": {"kind": "Pod", "apiVersion": "v1", "metadata": {"resourceVersion": "11020", ...}, ...}

}

...

给定的Kubernetes服务器将仅在有限时间内保留历史变更列表。使用etcd3的群集默认情况下会在最近5分钟内保留更改。当请求的监视操作因该资源的历史版本不可用而失败时，客户端必须通过识别状态代码410 Gone，清除其本地缓存，执行列表操作以及从该resourceVersion新列表操作返回的监视启动监视来处理该情况。大多数客户端库为此逻辑提供某种形式的标准工具。（在Go中，这称为a Reflector，位于k8s.io/client-go/cache包中。）

 

以块的形式检索大型结果集

在大型群集上，检索某些资源类型的集合可能会导致可能影响服务器和客户端的非常大的响应。例如，一个集群可能有数万个pod，每个pod都是1-2kb的编码JSON。检索所有命名空间中的所有pod可能会导致非常大的响应（10-20MB）并消耗大量服务器资源。从Kubernetes 1.9开始，服务器支持将单个大型收集请求分解为许多较小的块，同时保持总请求的一致性。每个块都可以按顺序返回，这样可以减少请求的总大小，并允许面向用户的客户端逐步显示结果以提高响应速度。

 

要以块的形式检索单个列表，将在集合请求中支持两个新参数，limit并从列表字段中的所有列表操作返回continue一个新字段。客户端应指定希望在每个块中接收的最大结果，并且服务器将返回结果中的资源，并且如果集合中有更多资源，则包含值。然后，客户端可以在下一个请求时将此值传递给服务器，以指示服务器返回下一个结果块。通过继续直到服务器返回空值，客户端可以使用完整的结果集。continuemetadatalimitlimitcontinuecontinuecontinue

 

与监视操作一样，continue令牌将在很短的时间后（默认为5分钟）到期，410 Gone如果无法返回更多结果，则返回一个令牌。在这种情况下，客户端将需要从头开始或省略limit参数。

 

例如，如果群集上有1,253个pod，并且客户端希望一次接收500个pod的块，他们将按如下方式请求这些块：

 

列出群集中的所有pod，每次最多检索500个pod。

 

GET /api/v1/pods?limit=500

---

200 OK

Content-Type: application/json

{

  "kind": "PodList",

  "apiVersion": "v1",

  "metadata": {

    "resourceVersion":"10245",

    "continue": "ENCODED_CONTINUE_TOKEN",

    ...

  },

  "items": [...] // returns pods 1-500

}

继续上一个呼叫，检索下一组500个pod。

 

GET /api/v1/pods?limit=500&continue=ENCODED_CONTINUE_TOKEN

---

200 OK

Content-Type: application/json

{

  "kind": "PodList",

  "apiVersion": "v1",

  "metadata": {

    "resourceVersion":"10245",

    "continue": "ENCODED_CONTINUE_TOKEN_2",

    ...

  },

  "items": [...] // returns pods 501-1000

}

继续上一个呼叫，检索最后253个窗格。

 

GET /api/v1/pods?limit=500&continue=ENCODED_CONTINUE_TOKEN_2

---

200 OK

Content-Type: application/json

{

  "kind": "PodList",

  "apiVersion": "v1",

  "metadata": {

    "resourceVersion":"10245",

    "continue": "", // continue token is empty because we have reached the end of the list

    ...

  },

  "items": [...] // returns pods 1001-1253

}

请注意，resourceVersion列表中的列表在每个请求中保持不变，表示服务器向我们显示了pod的一致快照。10245除非用户在没有continue令牌的情况下发出列表请求，否则将不会显示在版本之后创建，更新或删除的窗格。这允许客户端将大型请求分解为较小的块，然后对整个集执行监视操作，而不会丢失任何更新。

 

以表格形式接收资源

kubectl get是特定资源类型的一个或多个实例的简单表格表示。过去，客户需要重现表格并描述实现的输出kubectl以执行简单的对象列表。该方法的一些限制包括在处理某些对象时的非平凡逻辑。此外，API聚合或第三方资源提供的类型在编译时是未知的。这意味着必须为客户端无法识别的类型实现通用实现。

 

为了避免如上所述的潜在限制，客户端可以请求对象的表表示，将打印的特定细节委托给服务器。所述Kubernetes API实现了标准的HTTP内容类型的协商：传递一个Accept包含的头值application/json;as=Table;g=meta.k8s.io;v=v1beta1与GET呼叫将请求在表内容类型的服务器返回的对象。

 

例如：

 

以表格格式列出群集上的所有pod。

 

GET /api/v1/pods

Accept: application/json;as=Table;g=meta.k8s.io;v=v1beta1

---

200 OK

Content-Type: application/json

{

    "kind": "Table",

    "apiVersion": "meta.k8s.io/v1beta1",

    ...

    "columnDefinitions": [

        ...

    ]

}

对于服务器上没有自定义表定义的API资源类型，服务器返回默认的表响应，包括资源name和creationTimestamp字段。

 

    GET /apis/crd.example.com/v1alpha1/namespaces/default/resources

    ---

    200 OK

    Content-Type: application/json

    ...

    {

        "kind": "Table",

        "apiVersion": "meta.k8s.io/v1beta1",

        ...

        "columnDefinitions": [

            {

                "name": "Name",

                "type": "string",

                ...

            },

            {

                "name": "Created At",

                "type": "date",

                ...

            }

        ]

    }

表格响应可从kube-apiserver的1.10版开始提供。因此，并非所有API资源类型都支持Table响应，特别是在针对较旧的群集使用客户端时。必须针对所有资源类型工作或可能处理较旧群集的客户端应在其Accept标头中指定多个内容类型，以支持回退到非Tabular JSON：

 

Accept: application/json;as=Table;g=meta.k8s.io;v=v1beta1, application/json

资源的替代表示

默认情况下，Kubernetes将序列化为JSON的对象返回给内容类型application/json。这是API的默认序列化格式。但是，客户可以请求更高效的Protobuf表示这些对象，以获得更好的大规模性能。Kubernetes API实现标准HTTP内容类型协商：通过调用传递Accept标头GET将请求服务器返回提供的内容类型中的对象，同时将Protobuf中的对象发送到服务器以进行调用PUT或POST调用获取Content-Type标头。Content-Type如果支持所请求的格式，服务器将返回标头，如果提供406 Not acceptable了无效的内容类型，则返回错误。

 

有关每个API支持的内容类型的列表，请参阅API文档。

 

例如：

 

以Protobuf格式列出群集上的所有pod。

 

GET /api/v1/pods

Accept: application/vnd.kubernetes.protobuf

---

200 OK

Content-Type: application/vnd.kubernetes.protobuf

... binary encoded PodList object

通过将Protobuf编码数据发送到服务器来创建pod，但是请求JSON中的响应。

 

POST /api/v1/namespaces/test/pods

Content-Type: application/vnd.kubernetes.protobuf

Accept: application/json

... binary encoded Pod object

---

200 OK

Content-Type: application/json

{

  "kind": "Pod",

  "apiVersion": "v1",

  ...

}

并非所有API资源类型都支持Protobuf，特别是通过自定义资源定义或API扩展定义的那些。必须针对所有资源类型工作的客户端应在其Accept标头中指定多个内容类型以支持回退到JSON：

 

Accept: application/vnd.kubernetes.protobuf, application/json

Protobuf编码

Kubernetes使用信封包装来编码Protobuf响应。该包装器以4字节幻数开始，以帮助识别磁盘或etcd中的内容为Protobuf（而不是JSON），然后是Protobuf编码的包装器消息，它描述了底层对象的编码和类型，然后包含对象。

 

包装格式为：

 

A four byte magic number prefix:

  Bytes 0-3: "k8s\x00" [0x6b, 0x38, 0x73, 0x00]

 

An encoded Protobuf message with the following IDL:

  message Unknown {

    // typeMeta should have the string values for "kind" and "apiVersion" as set on the JSON object

    optional TypeMeta typeMeta = 1;

 

    // raw will hold the complete serialized object in protobuf. See the protobuf definitions in the client libraries for a given kind.

    optional bytes raw = 2;

 

    // contentEncoding is encoding used for the raw data. Unspecified means no encoding.

    optional string contentEncoding = 3;

 

    // contentType is the serialization method used to serialize 'raw'. Unspecified means application/vnd.kubernetes.protobuf and is usually

    // omitted.

    optional string contentType = 4;

  }

 

  message TypeMeta {

    // apiVersion is the group/version for this type

    optional string apiVersion = 1;

    // kind is the name of the object schema. A protobuf definition should exist for this object.

    optional string kind = 2;

  }

接收响应的客户端application/vnd.kubernetes.protobuf与预期的前缀不匹配应该拒绝响应，因为未来的版本可能需要以不兼容的方式更改序列化格式，并且将通过更改前缀来实现。

 

干运行

特征状态： Kubernetes v1.13 公测

在1.13版中，默认启用了干运行测试版功能。改性动词（POST，PUT，PATCH，和DELETE）可以接受在干燥运行模式的请求。干运行模式有助于通过典型的请求阶段（准入链，验证，合并冲突）评估请求，直到持久存储对象为止。请求的响应主体尽可能接近非干运行响应。系统保证干运行请求不会持久存储或具有任何其他副作用。

 

做一个干运行请求

通过设置dryRun查询参数来触发空运行。此参数是一个字符串，用作枚举，在1.13中，唯一可接受的值是：

 

All：除最终存储阶段外，每个阶段都正常运行。运行许可控制器以检查请求是否有效，变更控制器改变请求，执行合并PATCH，默认字段以及发生模式验证。这些更改不会持久保存到底层存储，但是仍然保留的最终对象仍会返回给用户以及正常的状态代码。如果请求将触发具有副作用的准入控制器，则该请求将失败而不是冒着不希望的副作用的风险。所有内置的准入控制插件都支持干运行。此外，许可webhooks可以在其配置对象中声明通过将sideEffects字段设置为“None”，它们没有副作用。如果webhook确实有副作用，那么sideEffects字段应设置为“NoneOnDryRun”，并且还应修改webhook以理解DryRunAdmissionReview中的字段，并防止对干运行请求产生副作用。

将值保留为空，这也是默认值：保持默认的修改行为。

例如：

 

    POST /api/v1/namespaces/test/pods?dryRun=All

    Content-Type: application/json

    Accept: application/json

响应看起来与非干运行请求相同，但某些生成字段的值可能不同。

 

生成的值

通常在持久化对象之前生成对象的某些值。重要的是不要依赖干运行请求设置的这些字段的值，因为这些值在干运行模式下可能与实际请求时不同。其中一些领域是：

 

name：如果generateName设置，name将具有唯一的随机名称

creationTimestamp/ deletionTimestamp：记录创建/删除的时间

UID：唯一标识对象并随机生成（非确定性）

resourceVersion：跟踪对象的持久版本

由变异准入控制器设置的任何字段

对于Service资源：kube-apiserver分配给v1.Service对象的端口或IP

服务器端应用

特征状态： Kubernetes v1.14 α

Server Side Apply允许除kubectl之外的客户端执行Apply操作，并最终完全替换仅存在于kubectl中的复杂客户端应用逻辑。如果启用了“服务器端应用”功能，则PATCH端点将接受其他application/apply-patch+yaml内容类型。Server Side Apply的用户可以将部分指定的对象发送到此端点。应用的配置应始终包含应用程序有意见的每个字段。

 

启用服务器端应用Alpha功能

Server Side Apply是一个alpha功能，因此默认情况下禁用它。要打开此功能门，您需要--feature-gates ServerSideApply=true在启动时包含该标志kube-apiserver。如果您有多个kube-apiserver副本，则所有副本都应具有相同的标志设置。

 

现场管理

与last-applied管理的注释相比kubectl，Server Side Apply使用更具声明性的方法来跟踪用户的字段管理，而不是用户的上次应用状态。这意味着，作为使用服务器端应用的副作用，有关哪个字段管理器管理对象中的每个字段的信息也变得可用。

 

对于用户来管理字段，在“服务器端应用”意义上，意味着用户依赖并期望字段的值不会更改。最后对字段值进行断言的用户将被记录为当前字段管理器。这可以通过使用POST，PUT或者不应用更改值PATCH，或者通过在发送到服务器端应用端点的配置中包含该字段来完成。尝试更改由其他人管理的字段的任何应用程序都将拒绝其请求（如果未强制，请参阅下面的“冲突”部分）。

 

字段管理存储在新引入的managedFields字段中，该字段是对象的一部分metadata。

 

Server Side Apply创建的对象的简单示例如下所示：

 

apiVersion: v1

kind: ConfigMap

metadata:

  name: test-cm

  namespace: default

  labels:

    test-label: test

  managedFields:

  - manager: kubectl

    operation: Apply

    apiVersion: v1

    fields:

      f:metadata:

        f:labels:

          f:test-label: {}

      f:data:

        f🔑 {}

data:

  key: some value

上面的对象包含一个管理器metadata.managedFields。管理器包含有关管理实体本身的基本信息，如操作类型，api版本以及由其管理的字段。

 

注意：此字段由apiserver管理，不应由用户更改。

然而，metadata.managedFields通过Update操作可以改变。这样做是非常气馁的，但如果managedFields进入不一致的状态（显然不应该发生），可能是一个合理的选择。

 

操作

此功能考虑的两种操作类型是Apply（PATCH带内容类型application/apply-patch+yaml）和Update（修改对象的所有其他操作）。两种操作都会更新managedFields，但行为略有不同。

 

例如，只有应用操作在冲突时失败，而更新则没有。此外，应用操作需要通过提供fieldManager查询参数来标识自己，而查询参数对于更新操作是可选的。

 

具有多个管理器的示例对象可能如下所示：

 

apiVersion: v1

kind: ConfigMap

metadata:

  name: test-cm

  namespace: default

  labels:

    test-label: test

  managedFields:

  - manager: kubectl

    operation: Apply

    apiVersion: v1

    fields:

      f:metadata:

        f:labels:

          f:test-label: {}

  - manager: kube-controller-manager

    operation: Update

    apiVersion: v1

    time: '2019-03-30T16:00:00.000Z'

    fields:

      f:data:

        f🔑 {}

data:

  key: new value

在此示例中，第二个操作Update由管理员调用kube-controller-manager。更新更改了数据字段中的值，导致字段的管理更改为kube-controller-manager。

 

注意：如果此更新是Apply操作，则由于所有权冲突，操作将失败。

合并策略

使用Server Side Apply实现的合并策略提供了通常更稳定的对象生命周期。服务器端应用尝试根据管理它们的事实合并字段，而不是仅根据值进行覆盖。这样，它可以使多个演员通过减少意外干扰来更容易和更稳定地更新同一个对象。

 

当用户将部分指定的对象发送到服务器端应用端点时，如果在两个位置都指定了服务器，则服务器会将其与支持应用配置中的值的活动对象合并。如果应用配置中存在的项目集不是上次由同一用户应用的项目的超集，则删除不由任何其他字段管理器管理的每个缺失项目。有关在合并时如何使用对象模式进行决策的更多信息，请参阅sigs.k8s.io/structured-merge-diff。

 

冲突

冲突是当Apply操作尝试更改另一个用户也声称要管理的字段时发生的特殊状态错误。这可以防止应用程序无意中覆盖其他用户设置的值。发生这种情况时，应用程序有3个选项来解决冲突：

 

覆盖值，成为唯一经理：如果有意覆盖值（或者如果应用程序是像控制器这样的自动化过程），则应用程序应将force查询参数设置为true并再次发出请求。这会强制操作成功，更改字段的值，并从managedField中的所有其他管理器条目中删除该字段。

不要覆盖值，放弃管理声明：如果应用程序不再关心字段的值，他们可以从配置中删除它并再次发出请求。这会使值保持不变，并导致从managedFields中的应用程序条目中删除该字段。

不要覆盖值，成为共享管理器：如果应用程序仍然关心字段的值，但不想覆盖它，则可以更改其配置中字段的值以匹配对象的值服务器，并再次发出请求。这使得值保持不变，并使得应用程序和已声称要管理它的所有其他现场管理员共享该字段的管理。

与客户端应用的比较

Server Side Apply实现冲突检测和解决的结果是，应用程序始终具有本地状态的最新字段值。如果他们不这样做，他们下次申请时就会发生冲突。解决冲突的三个选项中的任何一个都会导致应用的配置是服务器字段上对象的最新子集。

 

这与客户端应用不同，其中已被其他用户覆盖的过时值保留在应用程序的本地配置中。只有当用户更新该特定字段时，这些值才会变得准确，并且应用程序无法知道他们的下一个应用是否会覆盖其他用户的更改。

 

另一个区别是使用Client Side Apply的应用程序无法更改它们正在使用的API版本，但Server Side Apply支持此用例。

 

自定义资源

Server Side Apply当前将所有自定义资源视为非结构化数据。所有键的处理方式与struct字段相同，所有列表都被视为原子。将来，它将使用自定义资源定义中的验证字段，以允许自定义资源作者定义如何合并自己的对象。

 

 

3.3    客户端库

此页面包含使用各种编程语言的Kubernetes API的客户端库概述。

要使用Kubernetes REST API编写应用程序，您无需自己实现API调用和请求/响应类型。您可以将客户端库用于您正在使用的编程语言。

客户端库通常会为您执行常见任务，例如身份验证。大多数客户端库可以发现并使用Kubernetes服务帐户来验证API客户端是否在Kubernetes集群内运行，或者可以了解kubeconfig文件 格式以读取凭据和API服务器地址。

官方支持的Kubernetes**客户端库**

以下客户端库由Kubernetes SIG API Machinery正式维护。

  Language  	Client Library                         	Sample Programs
  Go        	github.com/kubernetes/client-go/       	browse         
  Python    	github.com/kubernetes-client/python/   	browse         
  Java      	github.com/kubernetes-client/java      	browse         
  dotnet    	github.com/kubernetes-client/csharp    	browse         
  JavaScript	github.com/kubernetes-client/javascript	browse         

社区维护的客户端库

以下Kubernetes API客户端库由其作者提供和维护，而不是由Kubernetes团队提供。

3.4    Kubernetes弃权政策

Kubernetes是一个包含许多组件和许多贡献者的大型系统。与任何此类软件一样，功能集自然会随着时间的推移而发展，有时可能需要删除功能。这可能包括API，标志甚至整个功能。为避免破坏现有用户，Kubernetes遵循系统中要删除的方面的弃用策略。

 

第4章    访问API

4.1    控制对Kubernetes API的访问

4.2    认证

4.3    使用Bootstrap令牌进行身份验证

4.4    使用准入控制器

4.5    动态准入控制

4.6    管理服务帐户

4.7    授权概述

4.8    使用RBAC授权

4.9    使用ABAC授权

4.10      使用节点授权

4.11      Webhook模式

第5章    API参考

5.1    众所周知的标签，注释和污点

5.2    V1.14

https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.14/

第6章    联邦API

6.1    extensions / v1beta1模型定义

6.2    extensions / v1beta1操作

6.3    federation / v1beta1模型定义

6.4    federation / v1beta1操作

6.5    v1模型定义

6.6    v1运营

第7章    设置工具参考

7.1    Kubeadm

7.1.1     kubeadm概述

7.1.2     kubeadm init

7.1.3     kubeadm加入

7.1.4     kubeadm升级

7.1.5     kubeadm配置

7.1.6     kubeadm重置

7.1.7     kubeadm令牌

7.1.8     kubeadm版本

7.1.9     kubeadm alpha

7.1.10 kubeadm初始阶段

7.1.11 kubeadm加入阶段

7.1.12 实施细节

7.2    kubefed

第8章    命令行工具参考

8.1    特色门

8.2    联合会-API服务器

8.3    联合会控制器经理

8.4    Kubelet身份验证/授权

 

8.5    TLS bootstrapping

TLS引导

在Kubernetes集群中，worker节点上的组件（kubelet和kube-proxy）需要与Kubernetes master组件通信，特别是kube-apiserver。为了确保通信保持私密，不受干扰，并确保群集的每个组件与另一个受信任组件通信，我们强烈建议在节点上使用客户端TLS证书。

引导这些组件的正常过程，特别是需要证书的工作节点，因此它们可以与kube-apiserver安全地通信，这可能是一个具有挑战性的过程，因为它通常超出了Kubernetes的范围，需要大量的额外工作。反过来，这可能会使初始化或扩展集群变得具有挑战性。

为了简化流程，从版本1.4开始，Kubernetes引入了证书请求和签名API以简化流程。该提案可以在这里找到。

本文档描述了节点初始化的过程，如何为kubelet设置TLS客户端证书引导以及它是如何工作的。

   初始化过程

   组态

   证书颁发机构

   kube-apiserver配置

   kube-controller-manager配置

   kubelet配置

   其他验证组件

   kubectl批准

   范围

初始化过程

当工作节点启动时，kubelet执行以下操作：

\1.       寻找它的kubeconfig文件

\2.       检索API服务器的URL和凭据，通常是kubeconfig文件中的TLS密钥和签名证书

\3.       尝试使用凭据与API服务器通信。

假设kube-apiserver成功验证了kubelet的凭证，它会将kubelet视为有效节点，并开始为其分配pod。

请注意，上述过程取决于：

   在本地主机上存在密钥和证书 kubeconfig

   证书已由kube-apiserver信任的证书颁发机构（CA）签名

以下所有内容都是设置和管理集群的人员的责任：

\1.       创建CA密钥和证书

\2.       将CA证书分发到正在运行kube-apiserver的主节点

\3.       为每个kubelet创建密钥和证书; 强烈建议每个kubelet都有一个独特的CN

\4.       使用CA密钥签署kubelet证书

\5.       将kubelet密钥和签名证书分发到运行kubelet的特定节点

本文档中描述的TLS Bootstrapping旨在简化，部分甚至完全自动化第3步，因为这些是初始化或扩展群集时最常见的。

Bootstrap初始化

在引导程序初始化过程中，会发生以下情况：

\1.       kubelet开始了

\2.       kubelet认为，它并没有有一个kubeconfig文件

\3.       kubelet搜索并查找bootstrap-kubeconfig文件

\4.       kubelet读取其引导文件，检索API服务器的URL和有限使用“令牌”

\5.       kubelet连接到API服务器，使用令牌进行身份验证

\6.       kubelet现在具有创建和检索证书签名请求（CSR）的有限凭据

\7.       kubelet为自己创建了一个CSR

\8.       CSR通过以下两种方式之一获得批准：

   如果已配置，kube-controller-manager将自动批准CSR

   如果已配置，则外部流程（可能是人员）使用Kubernetes API或通过批准CSR kubectl

\9.       为kubelet创建证书

\10.    证书颁发给kubelet

\11.    kubelet检索证书

\12.    kubelet kubeconfig使用密钥和签名证书创建一个正确的

\13.    kubelet开始正常运作

\14.    可选：如果已配置，则当证书接近到期时，kubelet会自动请求续订证书

\15.    续订证书将根据配置自动或手动批准和颁发。

本文档的其余部分介绍了配置TLS Bootstrapping的必要步骤及其局限性。

组态

要配置TLS引导和可选的自动批准，必须在以下组件上配置选项：

   KUBE-API服务器

   KUBE-控制器经理

   kubelet

   集群内资源：ClusterRoleBinding可能ClusterRole

此外，您还需要Kubernetes证书颁发机构（CA）。

证书颁发机构

如果没有自举，您将需要证书颁发机构（CA）密钥和证书。由于没有自举，这些将用于签署kubelet证书。和以前一样，您有责任将它们分发到主节点。

出于本文档的目的，我们假设这些已经/var/lib/kubernetes/ca.pem（证书）和/var/lib/kubernetes/ca-key.pem（密钥）分发给主节点。我们将这些称为“Kubernetes CA证书和密钥”。

所有使用这些证书的Kubernetes组件 - kubelet，kube-apiserver，kube-controller-manager--都假定密钥和证书是PEM编码的。

kube-apiserver**配置**

kube-apiserver有几个要求启用TLS引导：

识别签署客户端证书的CA.

将bootstrapping kubelet验证到system:bootstrappers组

授权bootstrapping kubelet创建证书签名请求（CSR）

识别客户证书

这适用于所有客户端证书身份验证。如果尚未设置，请将--client-ca-file=FILENAME标志添加到kube-apiserver命令以启用客户端证书身份验证，例如，引用包含签名证书的证书颁发机构捆绑包 --client-ca-file=/var/lib/kubernetes/ca.pem。

 

初始引导程序验证

为了使bootstrapping kubelet连接到kube-apiserver并请求证书，它必须首先向服务器进行身份验证。你可以使用任何身份验证，可以验证kubelet。

 

虽然任何身份验证策略都可用于kubelet的初始引导凭据，但建议使用以下两个身份验证器以便于配置。

 

Bootstrap令牌 - 测试版

令牌认证文件

Bootstrap令牌是一种更简单且更易于管理的方法来验证kubelet，并且在启动kube-apiserver时不需要任何额外的标志。使用bootstrap令牌目前是Kubernetes 1.12版的测试版。

 

无论您选择哪种方法，都要求kubelet能够作为具有以下权限的用户进行身份验证：

 

创建和检索CSR

如果启用了自动批准，则自动批准请求节点客户端证书。

使用引导令牌进行身份验证的kubelet将作为组中的用户进行身份验证system:bootstrappers，这是使用的标准方法。

 

随着此功能的成熟，您应该确保令牌绑定到基于角色的访问控制（RBAC）策略，该策略将请求（使用引导令牌）严格限制为与证书配置相关的客户端请求。通过RBAC，将令牌范围限定为一组可以实现极大的灵活性。例如，您可以在完成配置节点后禁用特定引导程序组的访问权限。

 

Bootstrap令牌

这里详细描述了Bootstrap令牌。这些是作为Kubernetes集群中的秘密存储的令牌，然后发布到单个kubelet。您可以为整个群集使用单个令牌，也可以为每个工作节点发出一个令牌。

 

这个过程有两个方面：

 

使用令牌ID，密钥和范围创建Kubernetes密钥。

将令牌发送到kubelet

从kubelet的角度来看，一个标记就像另一个标记，并没有特殊含义。然而，从kube-apiserver的角度来看，引导令牌是特殊的。由于它Type，namespace并且name，KUBE-API服务器将其识别为一个特殊的记号，并授予任何与该令牌特殊的启动权认证，特别是对待他们的成员system:bootstrappers组。这满足了TLS引导的基本要求。

 

有关创建秘密的详细信息，请访问此处。

 

如果要使用引导令牌，则必须在带有标志的kube-apiserver上启用它：

 

--enable-bootstrap-token-auth=true

令牌认证文件

kube-apiserver具有接受令牌作为身份验证的能力。这些令牌是任意的，但应该代表从安全随机数生成器（例如/dev/urandom在大多数现代Linux系统上）派生的至少128位熵。您可以通过多种方式生成令牌。例如：

 

head -c 16 /dev/urandom | od -An -t x | tr -d ' '

将生成看起来像的令牌02b50b05283e98dd0fd71db496ef01e8。

 

令牌文件应类似于以下示例，其中前三个值可以是任何值，引用的组名称应如下所示：

 

02b50b05283e98dd0fd71db496ef01e8,kubelet-bootstrap,10001,"system:bootstrappers"

将--token-auth-file=FILENAME标志添加到kube-apiserver命令（可能在您的systemd单元文件中）以启用令牌文件。有关详细信息，请参阅此处的文档 。

 

授权kubelet创建CSR

既然bootstrapping节点作为组的一部分进行了身份验证system:bootstrappers，则需要授权它创建证书签名请求（CSR）并在完成后检索它。幸运的是，Kubernetes正好附带了ClusterRole这些（以及这些）权限system:node-bootstrapper。

 

为此，您只需创建一个ClusterRoleBinding将system:bootstrappers组绑定到群集角色的方法system:node-bootstrapper。

 

# enable bootstrapping nodes to create CSR

apiVersion: rbac.authorization.k8s.io/v1

kind: ClusterRoleBinding

metadata:

  name: create-csrs-for-bootstrapping

subjects:

- kind: Group

  name: system:bootstrappers

  apiGroup: rbac.authorization.k8s.io

roleRef:

  kind: ClusterRole

  name: system:node-bootstrapper

  apiGroup: rbac.authorization.k8s.io

kube-controller-manager配置

当apiserver从kubelet接收证书请求并验证这些请求时，控制器管理器负责发出实际签名的证书。

 

控制器管理器通过证书发布控制循环执行此功能。这采用使用磁盘资产的cfssl本地签名者的形式 。目前，所有颁发的证书都有一年的有效期和一组默认的关键用法。

 

为了让控制器管理器签署证书，它需要以下内容：

 

访问您创建和分发的“Kubernetes CA密钥和证书”

实现CSR签名

访问密钥和证书

如前所述，您需要创建Kubernetes CA密钥和证书，并将其分发到主节点。控制器管理器将使用这些来签署kubelet证书。

 

由于这些签名证书将由kubelet用作对kube-apiserver的常规kubelet进行身份验证，因此在此阶段提供给控制器管理器的CA也必须由kube-apiserver信任以进行身份验证。这是通过标志--client-ca-file=FILENAME（例如--client-ca-file=/var/lib/kubernetes/ca.pem）提供给kube-apiserver ，如kube-apiserver配置部分所述。

 

要向Kube-controller-manager提供Kubernetes CA密钥和证书，请使用以下标志：

 

--cluster-signing-cert-file="/etc/path/to/kubernetes/ca/ca.crt" --cluster-signing-key-file="/etc/path/to/kubernetes/ca/ca.key"

例如：

 

--cluster-signing-cert-file="/var/lib/kubernetes/ca.pem" --cluster-signing-key-file="/var/lib/kubernetes/ca-key.pem"

签名证书的有效期可以使用flag配置：

 

--experimental-cluster-signing-duration

赞同

为了批准CSR，您需要告诉控制器管理员批准它们是可以接受的。这是通过向正确的组授予RBAC权限来完成的。

 

有两组不同的权限：

 

nodeclient：如果节点正在为节点创建新证书，则它还没有证书。它使用上面列出的一个令牌进行身份验证，因此是该群组的一部分system:bootstrappers。

selfnodeclient：如果某个节点正在续订其证书，那么它已经拥有一个证书（根据定义），它会连续使用该证书作为该组的一部分进行身份验证system:nodes。

要使kubelet能够请求和接收新证书，请创建一个ClusterRoleBinding将引导节点所属的组绑定system:bootstrappers到ClusterRole授予其权限的组，即system:certificates.k8s.io:certificatesigningrequests:nodeclient：

 

# Approve all CSRs for the group "system:bootstrappers"

apiVersion: rbac.authorization.k8s.io/v1

kind: ClusterRoleBinding

metadata:

  name: auto-approve-csrs-for-group

subjects:

- kind: Group

  name: system:bootstrappers

  apiGroup: rbac.authorization.k8s.io

roleRef:

  kind: ClusterRole

  name: system:certificates.k8s.io:certificatesigningrequests:nodeclient

  apiGroup: rbac.authorization.k8s.io

要使kubelet能够更新自己的客户端证书，请创建一个ClusterRoleBinding将完全正常运行的节点所属的组绑定到该组的权限，system:nodes以ClusterRole授予其权限，system:certificates.k8s.io:certificatesigningrequests:selfnodeclient：

 

# Approve renewal CSRs for the group "system:nodes"

apiVersion: rbac.authorization.k8s.io/v1

kind: ClusterRoleBinding

metadata:

  name: auto-approve-renewals-for-nodes

subjects:

- kind: Group

  name: system:nodes

  apiGroup: rbac.authorization.k8s.io

roleRef:

  kind: ClusterRole

  name: system:certificates.k8s.io:certificatesigningrequests:selfnodeclient

  apiGroup: rbac.authorization.k8s.io

注意：Kubernetes低于1.8：如果您运行的是早期版本的Kubernetes，特别是低于1.8的版本，则默认情况下不会发送上面引用的集群角色。你将不得不自己创建他们除了对ClusterRoleBindings上市。

 

要创建ClusterRoles：

 

# A ClusterRole which instructs the CSR approver to approve a user requesting

# node client credentials.

apiVersion: rbac.authorization.k8s.io/v1

kind: ClusterRole

metadata:

  name: system:certificates.k8s.io:certificatesigningrequests:nodeclient

rules:

- apiGroups: ["certificates.k8s.io"]

  resources: ["certificatesigningrequests/nodeclient"]

  verbs: ["create"]

---

# A ClusterRole which instructs the CSR approver to approve a node renewing its

# own client credentials.

apiVersion: rbac.authorization.k8s.io/v1

kind: ClusterRole

metadata:

  name: system:certificates.k8s.io:certificatesigningrequests:selfnodeclient

rules:

- apiGroups: ["certificates.k8s.io"]

  resources: ["certificatesigningrequests/selfnodeclient"]

  verbs: ["create"]

在csrapproving附带的一部分控制器 KUBE-控制器管理器和默认情况下启用。控制器使用SubjectAccessReview API来确定给定用户是否有权请求CSR，然后根据授权结果进行批准。为防止与其他批准者发生冲突，内置审批者未明确拒绝CSR。它只会忽略未经授权的请求。控制器还会将过期的证书修剪为垃圾收集的一部分。

 

kubelet配置

最后，在主节点正确设置并且所有必要的身份验证和授权到位后，我们可以配置kubelet。

 

kubelet需要以下配置来引导：

 

存储它生成的密钥和证书的路径（可选，可以使用默认值）

指向kubeconfig尚不存在的文件的路径; 它会将bootstrapped配置文件放在这里

引导kubeconfig文件的路径，用于提供服务器和引导凭证的URL，例如引导令牌

可选：轮换证书的说明

例如，引导程序kubeconfig应该位于kubelet可用的路径中/var/lib/kubelet/bootstrap-kubeconfig。

 

其格式与普通kubeconfig文件相同。示例文件可能如下所示：

 

apiVersion: v1

kind: Config

clusters:

- cluster:

    certificate-authority: /var/lib/kubernetes/ca.pem

    server: https://my.server.example.com:6443

  name: bootstrap

contexts:

- context:

    cluster: bootstrap

    user: kubelet-bootstrap

  name: bootstrap

current-context: bootstrap

preferences: {}

users:

- name: kubelet-bootstrap

  user:

    token: 07401b.f395accd246ae52d

需要注意的重要因素是：

 

certificate-authority：CA文件的路径，用于验证kube-apiserver提供的服务器证书

server：kube-apiserver的URL

token：要使用的令牌

令牌的格式无关紧要，只要它与kube-apiserver所期望的匹配即可。在上面的示例中，我们使用了引导令牌。如前所述，可以使用任何有效的身份验证方法，而不仅仅是令牌。

 

因为引导程序kubeconfig 是标准的kubeconfig，所以您可以使用kubectl它来生成它。要创建上面的示例文件：

 

kubectl config --kubeconfig=/var/lib/kubelet/bootstrap-kubeconfig set-cluster bootstrap --server='https://my.server.example.com:6443' --certificate-authority=/var/lib/kubernetes/ca.pem

kubectl config --kubeconfig=/var/lib/kubelet/bootstrap-kubeconfig set-credentials kubelet-bootstrap --token=07401b.f395accd246ae52d

kubectl config --kubeconfig=/var/lib/kubelet/bootstrap-kubeconfig set-context bootstrap --user=kubelet-bootstrap --cluster=bootstrap

kubectl config --kubeconfig=/var/lib/kubelet/bootstrap-kubeconfig use-context bootstrap

要指示kubelet使用引导程序kubeconfig，请使用以下kubelet标志：

 

--bootstrap-kubeconfig="/var/lib/kubelet/bootstrap-kubeconfig" --kubeconfig="/var/lib/kubelet/kubeconfig"

启动kubelet时，如果指定的文件--kubeconfig不存在，则指定via的bootstrap kubeconfig --bootstrap-kubeconfig用于从API服务器请求客户端证书。在通过kubelet批准证书请求和回执时，引用生成的密钥和获得的证书的kubeconfig文件将写入由指定的路径--kubeconfig。证书和密钥文件将放在指定的目录中--cert-dir。

 

客户和服务证书

以上所有内容都涉及kubelet 客户端证书，特别是kubelet用于向kube-apiserver进行身份验证的证书。

 

kubelet也可以使用服务证书。kubelet本身为某些功能公开了https端点。为了保护这些，kubelet可以做以下之一：

 

使用提供的密钥和证书，通过--tls-private-key-file和--tls-cert-file标志

如果未提供密钥和证书，则创建自签名密钥和证书

请求通过CSR API从群集服务器提供服务证书

默认情况下，TLS引导提供的客户端证书client auth仅用于签名，因此不能用作服务证书，或者server auth。

 

但是，您可以通过证书轮换至少部分地启用其服务器证书。

 

证书轮换

Kubernetes v1.8及更高版本的kubelet实现了beta功能，可以启用其客户端和/或服务证书的轮换。这些可以通过kubelet上的相应RotateKubeletClientCertificate和 RotateKubeletServerCertificate功能标志启用，默认情况下启用。

 

RotateKubeletClientCertificate导致kubelet通过在其现有凭据到期时创建新CSR来轮换其客户端证书。要启用此功能，请将以下标志传递给kubelet：

 

--rotate-certificates

RotateKubeletServerCertificate导致kubelet 都以自举其客户端凭证后，请求服务的证书和旋转该证书。要启用此功能，请将以下标志传递给kubelet：

 

--rotate-server-certificates

注意：出于安全原因，在核心Kubernetes中实施的CSR批准控制器不批准节点服务证书。要使用 运营商，需要运行自定义批准控制器，或手动批准服务证书请求。RotateKubeletServerCertificate

其他验证组件

本文档中描述的所有TLS引导都涉及kubelet。但是，其他组件可能需要直接与kube-apiserver通信。值得注意的是kube-proxy，它是Kubernetes控制平面的一部分并在每个节点上运行，但也可能包括其他组件，如监控或网络。

 

与kubelet一样，这些其他组件也需要一种对kube-apiserver进行身份验证的方法。您有几种生成这些凭据的选项：

 

旧方法：创建和分发证书的方式与在TLS引导之前对kubelet的方式相同

DaemonSet：由于kubelet本身在每个节点上加载，并且足以启动基本服务，因此您可以运行kube-proxy和其他特定于节点的服务，而不是作为独立进程运行，而是作为kube-system命名空间中的守护进程运行。由于它将是集群内的，因此您可以为其提供具有适当权限的适当服务帐户以执行其活动。这可能是配置此类服务的最简单方法。

kubectl批准

CSR可以在内置于控制器管理器的批准流程之外批准。

 

签名控制器不会立即签署所有证书请求。相反，它等待，直到他们被适当特权的用户标记为“已批准”状态。此流程旨在允许由外部审批控制器或核心控制器管理器中实施的审批控制器处理的自动审批。但是，群集管理员也可以使用kubectl手动批准证书请求。管理员可以列出CSR kubectl get csr并详细描述kubectl describe csr <name>。管理员可以使用kubectl certificate approve <name>和批准或拒绝CSR kubectl certificate deny <name>。

 

范围

尽管Kubernetes支持在容器中运行控制平面主控组件（如kube-apiserver和kube-controller-manager），甚至作为Podkubelet中的s，但在撰写本文时，您不能同时使用TLS Bootstrap kubelet并在其上运行主平面组件。

 

这种限制的原因是kubelet尝试在启动任何pod 之前引导与kube-apiserver的通信，甚至是在磁盘上定义的静态并通过kubelet选项引用的--pod-manifest-path=<PATH>。尝试在kubelet中同时执行TLS Bootstrapping和主组件会导致竞争条件：kubelet需要与kube-apiserver通信以请求证书，但需要这些证书才能启动kube-apiserver。

 

一个问题是在这里公开引用它。

 

 

 

8.6    云控制器经理

8.7    KUBE-API服务器

8.8    KUBE-控制器经理

8.9    KUBE-代理

8.10      KUBE-调度

8.11      kubelet

第9章    kubectl CLI

9.1    kubectl概述

Kubectl是一个命令行界面，用于运行针对Kubernetes集群的命令。kubectl在$ HOME / .kube目录中查找名为config的文件。您可以通过设置KUBECONFIG环境变量或设置标志来指定其他kubeconfig文件--kubeconfig。

此概述涵盖kubectl语法，描述命令操作，并提供常见示例。有关每个命令的详细信息，包括所有支持的标志和子命令，请参阅kubectl参考文档。有关安装说明，请参阅安装kubectl。

9.2    JSONPath支持

Kubectl支持JSONPath模板。

JSONPath模板由大括号{}括起的JSONPath表达式组成。Kubectl使用JSONPath表达式过滤JSON对象中的特定字段并格式化输出。除了原始的JSONPath模板语法之外，以下函数和语法也是有效的：

\1.       使用双引号引用JSONPath表达式中的文本。

\2.       使用range，end运算符迭代列表。

\3.       使用负片索引向后退一个列表。负指数不会“环绕”一个列表，只要有效就有效-index + listLength >= 0。

注意：

   $操作者是可选的，因为表达总是默认从根对象开始。

   结果对象打印为String（）函数。

给定JSON输入：

{

  "kind": "List",

  "items":[

    {

      "kind":"None",

      "metadata":{"name":"127.0.0.1"},

      "status":{

        "capacity":{"cpu":"4"},

        "addresses":[{"type": "LegacyHostIP", "address":"127.0.0.1"}]

      }

    },

    {

      "kind":"None",

      "metadata":{"name":"127.0.0.2"},

      "status":{

        "capacity":{"cpu":"8"},

        "addresses":[

          {"type": "LegacyHostIP", "address":"127.0.0.2"},

          {"type": "another", "address":"127.0.0.3"}

        ]

      }

    }

  ],

  "users":[

    {

      "name": "myself",

      "user": {}

    },

    {

      "name": "e2e",

      "user": {"username": "admin", "password": "secret"}

    }

  ]

}

 

  Function           	Description                	Example                                 	Result                                  
  text               	the plain text             	kind is {.kind}                         	kind is List                            
  @                  	the current   object       	{@}                                     	the same as   input                     
  . or []            	child operator             	{.kind} or {[‘kind’]}                   	List                                    
  ..                 	recursive   descent        	{..name}                                	127.0.0.1   127.0.0.2 myself e2e        
  *                  	wildcard. Get   all objects	{.items[*].metadata.name}               	[127.0.0.1   127.0.0.2]                 
  [start:end   :step]	subscript   operator       	{.users[0].name}                        	myself                                  
  [,]                	union operator             	{.items*}                               	127.0.0.1   127.0.0.2 map[cpu:4] map[cpu:8]
  ?()                	filter                     	{.users[?(@.name==“e2e”)].user.password}	secret                                  
  range, end         	iterate list               	{range   .items[*]}[{.metadata.name}, {.status.capacity}] {end}	[127.0.0.1,   map[cpu:4]] [127.0.0.2, map[cpu:8]]
  “                  	quote   interpreted string 	{range   .items[*]}{.metadata.name}{’\t’}{end}	127.0.0.1   127.0.0.2                   

使用kubectl和JSONPath表达式的示例：

kubectl get pods -o json

kubectl get pods -o=jsonpath='{@}'

kubectl get pods -o=jsonpath='{.items[0]}'

kubectl get pods -o=jsonpath='{.items[0].metadata.name}'

kubectl get pods -o=jsonpath='{range .items[*]}{.metadata.name}{"\t"}{.status.startTime}{"\n"}{end}'

在Windows上，您必须双引号包含空格的任何JSONPath模板（不是单引号，如上所示为bash）。这反过来意味着您必须在模板中的任何文字周围使用单引号或转义双引号。例如：

C:> kubectl get pods -o=jsonpath="{range .items[*]}{.metadata.name}{'\t'}{.status.startTime}{'\n'}{end}"

C:> kubectl get pods -o=jsonpath="{range .items[*]}{.metadata.name}{"\t"}{.status.startTime}{"\n"}{end}"

9.3    kubectl

kubectl

kubectl controls the Kubernetes cluster manager

 

Synopsis

kubectl controls the Kubernetes cluster manager.

 

Find more information at: https://kubernetes.io/docs/reference/kubectl/overview/

 

kubectl [flags]

Options

--alsologtostderr

log to standard error as well as files

--application-metrics-count-limit int     Default: 100

Max number of application metrics to store (per container)

--as string

Username to impersonate for the operation

--as-group stringArray

Group to impersonate for the operation, this flag can be repeated to specify multiple groups.

--azure-container-registry-config string

Path to the file containing Azure container registry configuration information.

--boot-id-file string     Default: "/proc/sys/kernel/random/boot_id"

Comma-separated list of files to check for boot-id. Use the first one that exists.

--cache-dir string     Default: "/home/tengqm/.kube/http-cache"

Default HTTP cache directory

--certificate-authority string

Path to a cert file for the certificate authority

--client-certificate string

Path to a client certificate file for TLS

--client-key string

Path to a client key file for TLS

--cloud-provider-gce-lb-src-cidrs cidrs     Default: 130.211.0.0/22,209.85.152.0/22,209.85.204.0/22,35.191.0.0/16

CIDRs opened in GCE firewall for LB traffic proxy & health checks

--cluster string

The name of the kubeconfig cluster to use

--container-hints string     Default: "/etc/cadvisor/container_hints.json"

location of the container hints file

--containerd string     Default: "unix:///var/run/containerd.sock"

containerd endpoint

--context string

The name of the kubeconfig context to use

--default-not-ready-toleration-seconds int     Default: 300

Indicates the tolerationSeconds of the toleration for notReady:NoExecute that is added by default to every pod that does not already have such a toleration.

--default-unreachable-toleration-seconds int     Default: 300

Indicates the tolerationSeconds of the toleration for unreachable:NoExecute that is added by default to every pod that does not already have such a toleration.

--docker string     Default: "unix:///var/run/docker.sock"

docker endpoint

--docker-env-metadata-whitelist string

a comma-separated list of environment variable keys that needs to be collected for docker containers

--docker-only

Only report docker containers in addition to root stats

--docker-root string     Default: "/var/lib/docker"

DEPRECATED: docker root is read from docker info (this is a fallback, default: /var/lib/docker)

--docker-tls

use TLS to connect to docker

--docker-tls-ca string     Default: "ca.pem"

path to trusted CA

--docker-tls-cert string     Default: "cert.pem"

path to client certificate

--docker-tls-key string     Default: "key.pem"

path to private key

--enable-load-reader

Whether to enable cpu load reader

--event-storage-age-limit string     Default: "default=0"

Max length of time for which to store events (per type). Value is a comma separated list of key values, where the keys are event types (e.g.: creation, oom) or "default" and the value is a duration. Default is applied to all non-specified event types

--event-storage-event-limit string     Default: "default=0"

Max number of events to store (per type). Value is a comma separated list of key values, where the keys are event types (e.g.: creation, oom) or "default" and the value is an integer. Default is applied to all non-specified event types

--global-housekeeping-interval duration     Default: 1m0s

Interval between global housekeepings

-h, --help

help for kubectl

--housekeeping-interval duration     Default: 10s

Interval between container housekeepings

--insecure-skip-tls-verify

If true, the server's certificate will not be checked for validity. This will make your HTTPS connections insecure

--kubeconfig string

Path to the kubeconfig file to use for CLI requests.

--log-backtrace-at traceLocation     Default: :0

when logging hits line file:N, emit a stack trace

--log-cadvisor-usage

Whether to log the usage of the cAdvisor container

--log-dir string

If non-empty, write log files in this directory

--log-file string

If non-empty, use this log file

--log-flush-frequency duration     Default: 5s

Maximum number of seconds between log flushes

--logtostderr     Default: true

log to standard error instead of files

--machine-id-file string     Default: "/etc/machine-id,/var/lib/dbus/machine-id"

Comma-separated list of files to check for machine-id. Use the first one that exists.

--match-server-version

Require server version to match client version

--mesos-agent string     Default: "127.0.0.1:5051"

Mesos agent address

--mesos-agent-timeout duration     Default: 10s

Mesos agent timeout

-n, --namespace string

If present, the namespace scope for this CLI request

--password string

Password for basic authentication to the API server

--profile string     Default: "none"

Name of profile to capture. One of (none|cpu|heap|goroutine|threadcreate|block|mutex)

--profile-output string     Default: "profile.pprof"

Name of the file to write the profile to

--request-timeout string     Default: "0"

The length of time to wait before giving up on a single server request. Non-zero values should contain a corresponding time unit (e.g. 1s, 2m, 3h). A value of zero means don't timeout requests.

-s, --server string

The address and port of the Kubernetes API server

--skip-headers

If true, avoid header prefixes in the log messages

--stderrthreshold severity     Default: 2

logs at or above this threshold go to stderr

--storage-driver-buffer-duration duration     Default: 1m0s

Writes in the storage driver will be buffered for this duration, and committed to the non memory backends as a single transaction

--storage-driver-db string     Default: "cadvisor"

database name

--storage-driver-host string     Default: "localhost:8086"

database host:port

--storage-driver-password string     Default: "root"

database password

--storage-driver-secure

use secure connection with database

--storage-driver-table string     Default: "stats"

table name

--storage-driver-user string     Default: "root"

database username

--token string

Bearer token for authentication to the API server

--update-machine-info-interval duration     Default: 5m0s

Interval between machine info updates.

--user string

The name of the kubeconfig user to use

--username string

Username for basic authentication to the API server

-v, --v Level

number for the log level verbosity

--version version[=true]

Print version information and quit

--vmodule moduleSpec

comma-separated list of pattern=N settings for file-filtered logging

SEE ALSO

kubectl annotate - Update the annotations on a resource

kubectl api-resources - Print the supported API resources on the server

kubectl api-versions - Print the supported API versions on the server, in the form of “group/version”

kubectl apply - Apply a configuration to a resource by filename or stdin

kubectl attach - Attach to a running container

kubectl auth - Inspect authorization

kubectl autoscale - Auto-scale a Deployment, ReplicaSet, or ReplicationController

kubectl certificate - Modify certificate resources.

kubectl cluster-info - Display cluster info

kubectl completion - Output shell completion code for the specified shell (bash or zsh)

kubectl config - Modify kubeconfig files

kubectl convert - Convert config files between different API versions

kubectl cordon - Mark node as unschedulable

kubectl cp - Copy files and directories to and from containers.

kubectl create - Create a resource from a file or from stdin.

kubectl delete - Delete resources by filenames, stdin, resources and names, or by resources and label selector

kubectl describe - Show details of a specific resource or group of resources

kubectl diff - Diff live version against would-be applied version

kubectl drain - Drain node in preparation for maintenance

kubectl edit - Edit a resource on the server

kubectl exec - Execute a command in a container

kubectl explain - Documentation of resources

kubectl expose - Take a replication controller, service, deployment or pod and expose it as a new Kubernetes Service

kubectl get - Display one or many resources

kubectl kustomize - Build a kustomization target from a directory or a remote url.

kubectl label - Update the labels on a resource

kubectl logs - Print the logs for a container in a pod

kubectl options - Print the list of flags inherited by all commands

kubectl patch - Update field(s) of a resource using strategic merge patch

kubectl plugin - Provides utilities for interacting with plugins.

kubectl port-forward - Forward one or more local ports to a pod

kubectl proxy - Run a proxy to the Kubernetes API server

kubectl replace - Replace a resource by filename or stdin

kubectl rollout - Manage the rollout of a resource

kubectl run - Run a particular image on the cluster

kubectl scale - Set a new size for a Deployment, ReplicaSet, Replication Controller, or Job

kubectl set - Set specific features on objects

kubectl taint - Update the taints on one or more nodes

kubectl top - Display Resource (CPU/Memory/Storage) usage.

kubectl uncordon - Mark node as schedulable

kubectl version - Print the client and server version information

kubectl wait - Experimental: Wait for a specific condition on one or many resources.

Feedback

9.4    kubectl备忘单

9.4.1     Kubectl自动补全

BASH

# setup autocomplete in bash into the current shell, bash-completion package should be installed first.

source <(kubectl completion bash) 

# add autocomplete permanently to your bash shell.

echo "source <(kubectl completion bash)" >> ~/.bashrc 

您也可以使用速记别名kubectl，也可以使用完成：

alias k=kubectl

complete -F __start_kubectl k

ZSH

source <(kubectl completion zsh)  # setup autocomplete in zsh into the current shell

echo "if [ $commands[kubectl] ]; then source <(kubectl completion zsh); fi" >> ~/.zshrc # add autocomplete permanently to your zsh shell

Kubectl**上下文和配置**

设置与哪个Kubernetes集群kubectl通信并修改配置信息。有关详细的配置文件信息，请参阅使用kubeconfig文档验证跨群集。

kubectl config view # Show Merged kubeconfig settings.

 

# use multiple kubeconfig files at the same time and view merged config

KUBECONFIG=~/.kube/config:~/.kube/kubconfig2 

 

kubectl config view

 

# get the password for the e2e user

kubectl config view -o jsonpath='{.users[?(@.name == "e2e")].user.password}'

 

kubectl config view -o jsonpath='{.users[].name}'    # get a list of users

kubectl config get-contexts                          # display list of contexts 

kubectl config current-context                                     # display the current-context

kubectl config use-context my-cluster-name           # set the default context to my-cluster-name

 

# add a new cluster to your kubeconf that supports basic auth

kubectl config set-credentials kubeuser/foo.kubernetes.com --username=kubeuser --password=kubepassword

 

# permanently save the namespace for all subsequent kubectl commands in that context.

kubectl config set-context --current --namespace=ggckad-s2

 

# set a context utilizing a specific username and namespace.

kubectl config set-context gce --user=cluster-admin --namespace=foo \

  && kubectl config use-context gce

 

kubectl config unset users.foo                       # delete user foo

应用

apply通过定义Kubernetes资源的文件管理应用程序。它通过运行在集群中创建和更新资源kubectl apply。这是在生产中管理Kubernetes应用程序的推荐方法。见Kubectl Book。

创建对象

Kubernetes清单可以用json或yaml定义。文件扩展名.yaml， .yml以及.json可以使用。

kubectl apply -f ./my-manifest.yaml           # create resource(s)

kubectl apply -f ./my1.yaml -f ./my2.yaml     # create from multiple files

kubectl apply -f ./dir                        # create resource(s) in all manifest files in dir

kubectl apply -f https://git.io/vPieo         # create resource(s) from url

kubectl create deployment nginx --image=nginx  # start a single instance of nginx

kubectl explain pods,svc                       # get the documentation for pod and svc manifests

 

# Create multiple YAML objects from stdin

cat <<EOF | kubectl apply -f -

apiVersion: v1

kind: Pod

metadata:

  name: busybox-sleep

spec:

  containers:

  - name: busybox

    image: busybox

    args:

    - sleep

    - "1000000"

---

apiVersion: v1

kind: Pod

metadata:

  name: busybox-sleep-less

spec:

  containers:

  - name: busybox

    image: busybox

    args:

    - sleep

    - "1000"

EOF

 

# Create a secret with several keys

cat <<EOF | kubectl apply -f -

apiVersion: v1

kind: Secret

metadata:

  name: mysecret

type: Opaque

data:

  password: $(echo -n "s33msi4" | base64 -w0)

  username: $(echo -n "jane" | base64 -w0)

EOF

9.4.2     查看，查找资源

# Get commands with basic output

kubectl get services                          # List all services in the namespace

kubectl get pods --all-namespaces             # List all pods in all namespaces

kubectl get pods -o wide                      # List all pods in the namespace, with more details

kubectl get deployment my-dep                 # List a particular deployment

kubectl get pods --include-uninitialized      # List all pods in the namespace, including uninitialized ones

kubectl get pod my-pod -o yaml                # Get a pod's YAML

kubectl get pod my-pod -o yaml --export       # Get a pod's YAML without cluster specific information

 

# Describe commands with verbose output

kubectl describe nodes my-node

kubectl describe pods my-pod

 

kubectl get services --sort-by=.metadata.name # List Services Sorted by Name

 

# List pods Sorted by Restart Count

kubectl get pods --sort-by='.status.containerStatuses[0].restartCount'

 

# Get the version label of all pods with label app=cassandra

kubectl get pods --selector=app=cassandra rc -o \

  jsonpath='{.items[*].metadata.labels.version}'

 

# Get all worker nodes (use a selector to exclude results that have a label

# named 'node-role.kubernetes.io/master')

kubectl get node --selector='!node-role.kubernetes.io/master'

 

# Get all running pods in the namespace

kubectl get pods --field-selector=status.phase=Running

 

# Get ExternalIPs of all nodes

kubectl get nodes -o jsonpath='{.items[*].status.addresses[?(@.type=="ExternalIP")].address}'

 

# List Names of Pods that belong to Particular RC

# "jq" command useful for transformations that are too complex for jsonpath, it can be found at https://stedolan.github.io/jq/

sel=${$(kubectl get rc my-rc --output=json | jq -j '.spec.selector | to_entries | .[] | "(.key)=(.value),"')%?}

echo $(kubectl get pods --selector=$sel --output=jsonpath={.items..metadata.name})

 

# Show labels for all pods (or any other Kubernetes object that supports labelling)

# Also uses "jq"

for item in $( kubectl get pod --output=name); do printf "Labels for %s\n" "$item" | grep --color -E '/+$' && kubectl get "$item" --output=json | jq -r -S '.metadata.labels | to_entries | .[] | " (.key)=(.value)"' 2>/dev/null; printf "\n"; done

 

# Check which nodes are ready

JSONPATH='{range .items[]}{@.metadata.name}:{range @.status.conditions[]}{@.type}={@.status};{end}{end}' \

 && kubectl get nodes -o jsonpath="$JSONPATH" | grep "Ready=True"

 

# List all Secrets currently in use by a pod

kubectl get pods -o json | jq '.items[].spec.containers[].env[]?.valueFrom.secretKeyRef.name' | grep -v null | sort | uniq

 

# List Events sorted by timestamp

kubectl get events --sort-by=.metadata.creationTimestamp

9.4.3     更新资源

从版本1.11 rolling-update开始，不推荐使用（请参阅CHANGELOG-1.11.md）rollout。

kubectl set image deployment/frontend www=image:v2               # Rolling update "www" containers of "frontend" deployment, updating the image

kubectl rollout undo deployment/frontend                         # Rollback to the previous deployment

kubectl rollout status -w deployment/frontend                    # Watch rolling update status of "frontend" deployment until completion

 

# deprecated starting version 1.11

kubectl rolling-update frontend-v1 -f frontend-v2.json           # (deprecated) Rolling update pods of frontend-v1

kubectl rolling-update frontend-v1 frontend-v2 --image=image:v2  # (deprecated) Change the name of the resource and update the image

kubectl rolling-update frontend --image=image:v2                 # (deprecated) Update the pods image of frontend

kubectl rolling-update frontend-v1 frontend-v2 --rollback        # (deprecated) Abort existing rollout in progress

 

cat pod.json | kubectl replace -f -                              # Replace a pod based on the JSON passed into std

 

# Force replace, delete and then re-create the resource. Will cause a service outage.

kubectl replace --force -f ./pod.json

 

# Create a service for a replicated nginx, which serves on port 80 and connects to the containers on port 8000

kubectl expose rc nginx --port=80 --target-port=8000

 

# Update a single-container pod's image version (tag) to v4

kubectl get pod mypod -o yaml | sed 's/(image: myimage):.*$/\1:v4/' | kubectl replace -f -

 

kubectl label pods my-pod new-label=awesome                      # Add a Label

kubectl annotate pods my-pod icon-url=http://goo.gl/XXBTWq       # Add an annotation

kubectl autoscale deployment foo --min=2 --max=10                # Auto scale a deployment "foo"

修补资源

kubectl patch node k8s-node-1 -p '{"spec":{"unschedulable":true}}' # Partially update a node

 

# Update a container's image; spec.containers[*].name is required because it's a merge key

kubectl patch pod valid-pod -p '{"spec":{"containers":[{"name":"kubernetes-serve-hostname","image":"new image"}]}}'

 

# Update a container's image using a json patch with positional arrays

kubectl patch pod valid-pod --type='json' -p='[{"op": "replace", "path": "/spec/containers/0/image", "value":"new image"}]'

 

# Disable a deployment livenessProbe using a json patch with positional arrays

kubectl patch deployment valid-deployment  --type json   -p='[{"op": "remove", "path": "/spec/template/spec/containers/0/livenessProbe"}]'

 

# Add a new element to a positional array 

kubectl patch sa default --type='json' -p='[{"op": "add", "path": "/secrets/1", "value": {"name": "whatever" } }]'

编辑资源

编辑编辑器中的任何API资源。

kubectl edit svc/docker-registry                      # Edit the service named docker-registry

KUBE_EDITOR="nano" kubectl edit svc/docker-registry   # Use an alternative editor

扩展资源

kubectl scale --replicas=3 rs/foo                                 # Scale a replicaset named 'foo' to 3

kubectl scale --replicas=3 -f foo.yaml                            # Scale a resource specified in "foo.yaml" to 3

kubectl scale --current-replicas=2 --replicas=3 deployment/mysql  # If the deployment named mysql's current size is 2, scale mysql to 3

kubectl scale --replicas=5 rc/foo rc/bar rc/baz                   # Scale multiple replication controllers

删除资源

kubectl delete -f ./pod.json                                              # Delete a pod using the type and name specified in pod.json

kubectl delete pod,service baz foo                                        # Delete pods and services with same names "baz" and "foo"

kubectl delete pods,services -l name=myLabel                              # Delete pods and services with label name=myLabel

kubectl delete pods,services -l name=myLabel --include-uninitialized      # Delete pods and services, including uninitialized ones, with label name=myLabel

kubectl -n my-ns delete po,svc --all                                      # Delete all pods and services, including uninitialized ones, in namespace my-ns,

# Delete all pods matching the awk pattern1 or pattern2

kubectl get pods  -n mynamespace --no-headers=true | awk '/pattern1|pattern2/{print $1}' | xargs  kubectl delete -n mynamespace pod

与正在运行的Pod**交互**

kubectl logs my-pod                                 # dump pod logs (stdout)

kubectl logs -l name=myLabel                        # dump pod logs, with label name=myLabel (stdout)

kubectl logs my-pod --previous                      # dump pod logs (stdout) for a previous instantiation of a container

kubectl logs my-pod -c my-container                 # dump pod container logs (stdout, multi-container case)

kubectl logs -l name=myLabel -c my-container        # dump pod logs, with label name=myLabel (stdout)

kubectl logs my-pod -c my-container --previous      # dump pod container logs (stdout, multi-container case) for a previous instantiation of a container

kubectl logs -f my-pod                              # stream pod logs (stdout)

kubectl logs -f my-pod -c my-container              # stream pod container logs (stdout, multi-container case)

kubectl logs -f -l name=myLabel --all-containers    # stream all pods logs with label name=myLabel (stdout)

kubectl run -i --tty busybox --image=busybox -- sh  # Run pod as interactive shell

kubectl attach my-pod -i                            # Attach to Running Container

kubectl port-forward my-pod 5000:6000               # Listen on port 5000 on the local machine and forward to port 6000 on my-pod

kubectl exec my-pod -- ls /                         # Run command in existing pod (1 container case)

kubectl exec my-pod -c my-container -- ls /         # Run command in existing pod (multi-container case)

kubectl top pod POD_NAME --containers               # Show metrics for a given pod and its containers

与节点和集群交互

kubectl cordon my-node                                                # Mark my-node as unschedulable

kubectl drain my-node                                                 # Drain my-node in preparation for maintenance

kubectl uncordon my-node                                              # Mark my-node as schedulable

kubectl top node my-node                                              # Show metrics for a given node

kubectl cluster-info                                                  # Display addresses of the master and services

kubectl cluster-info dump                                             # Dump current cluster state to stdout

kubectl cluster-info dump --output-directory=/path/to/cluster-state   # Dump current cluster state to /path/to/cluster-state

 

# If a taint with that key and effect already exists, its value is replaced as specified.

kubectl taint nodes foo dedicated=special-user:NoSchedule

资源类型

列出所有支持的资源类型及其短名称，API组，是否为命名空间，以及Kind：

kubectl api-resources

用于探索API资源的其他操作：

kubectl api-resources --namespaced=true      # All namespaced resources

kubectl api-resources --namespaced=false     # All non-namespaced resources

kubectl api-resources -o name                # All resources with simple output (just the resource name)

kubectl api-resources -o wide                # All resources with expanded (aka "wide") output

kubectl api-resources --verbs=list,get       # All resources that support the "list" and "get" request verbs

kubectl api-resources --api-group=extensions # All resources in the "extensions" API group

格式化输出

要以特定格式将详细信息输出到终端窗口，可以向支持的命令添加-o或--output标记kubectl。

  输出格式                             	描述                             
  -o=custom-columns=<spec>         	使用逗号分隔的自定义列列表打印表               
  -o=custom-columns-file=<filename>	使用<filename>文件中的自定义列模板打印表      
  -o=json                          	输出JSON格式的API对象                 
  -o=jsonpath=<template>           	打印jsonpath表达式中定义的字段            
  -o=jsonpath-file=<filename>      	打印文件中jsonpath表达式定义的字段<filename>
  -o=name                          	仅打印资源名称而不打印任何其他内容              
  -o=wide                          	使用任何其他信息以纯文本格式输出，对于pod，包括节点名称  
  -o=yaml                          	输出YAML格式的API对象                 

Kubectl**输出详细程度和调试**

Kubectl详细程度由-v或--v标志后跟一个表示日志级别的整数控制。此处描述了一般Kubernetes日志记录约定和相关的日志级别。

  赘言   	描述                                      
  --v=0	通常对此有用，始终对操作员可见。                        
  --v=1	如果您不想要详细程度，则为合理的默认日志级别。                 
  --v=2	有关服务的有用稳态信息以及可能与系统中的重大更改相关的重要日志消息。这是大多数系统的建议默认日志级别。
  --v=3	有关更改的扩展信息。                              
  --v=4	调试级别详细程度。                               
  --v=6	显示请求的资源。                                
  --v=7	显示HTTP请求标头。                             
  --v=8	显示HTTP请求内容。                             
  --v=9	显示HTTP请求内容而不截断内容。                       

9.5    kubectl命令

https://kubernetes.io/docs/reference/generated/kubectl/kubectl-commands

https://kubectl.docs.kubernetes.io/

列出Kubernetes**资源**

列出kube-system命名空间中的Kubernetes 部署资源。

kubectl get deployments --namespace kube-system

在kube-system命名空间中打印有关kube-dns deployment的详细信息。

kubectl describe deployment kube-dns --namespace kube-system

从配置创建资源

从远程配置创建或更新Kubernetes资源。

kubectl apply -f https://raw.githubusercontent.com/kubernetes/kubectl/master/docs/book/examples/nginx/nginx.yaml

从本地配置创建或更新Kubernetes资源。

kubectl apply -f ./examples/nginx/nginx.yaml

打印已应用的资源。

kubectl get -f ./examples/nginx/nginx.yaml --show-labels

从命令生成配置

为部署资源生成配置。这可以通过将输出写入文件然后运行kubectl apply -f <yaml-file>来应用于集群。注意：生成的Config具有额外的样板，用户不应该包含但由于go对象的序列化过程而存在。

kubectl create deployment nginx --dry-run -o yaml --image nginx

apiVersion: apps/v1

kind: Deployment

metadata:

  creationTimestamp: null # delete this

  labels:

    app: nginx

  name: nginx

spec:

  replicas: 1

  selector:

    matchLabels:

      app: nginx

  strategy: {} # delete this

  template:

    metadata:

      creationTimestamp: null # delete this

      labels:

        app: nginx

    spec:

      containers:

      - image: nginx

        name: nginx

        resources: {} # delete this

status: {} # delete this

查看与资源关联的Pod

使用Pod标签查看Deployment创建的Pod。

kubectl get pods -l app=nginx

调试容器

从Deployment管理的所有Pod中获取日志。

kubectl logs -l app=nginx

将shell放入特定的Pod容器中

kubectl exec -i -t  nginx-deployment-5c689d88bb-s7xcv bash

9.6    kubectl使用惯例

使用kubectl**可重复使用的脚本**

对于脚本中的稳定输出：

   请求的面向机器的输出形式中的一种，例如-o name，-o json，-o yaml，-o go-template，或-o jsonpath。

   完全限定版本。例如，jobs.v1.batch/myjob。这将确保kubectl不使用可随时间变化的默认版本。

   --generator使用基于生成器的命令（如kubectl run或）时，指定要固定到特定行为的标志kubectl expose。

   不要依赖上下文，首选项或其他隐式状态。

最佳实践

kubectl run

为了kubectl run满足基础设施代码：

使用特定于版本的标记标记图像，不要将该标记移动到新版本。例如，使用:v1234，v1.2.3，r03062016-1-4，而不是:latest（有关详细信息，请参阅用于配置最佳实践）。

捕获签入脚本中的参数，或者至少用于使用--record命令行注释创建的对象，以便对参数化的图像进行轻微参数化。

检查脚本中是否有大量参数化的图像。

切换到检查到源代码管理中的配置文件，以获取所需的功能，但不能通过kubectl run标志表达。

固定到特定的生成器版本，例如kubectl run --generator=deployment/v1beta1。

Generators

您可以使用以下资源kubectl run与--generator标志：

  Resource                        	kubectl command                         
  Pod                             	kubectl run   --generator=run-pod/v1    
  Replication   controller        	kubectl run   --generator=run/v1        
  Deployment                      	kubectl run   --generator=extensions/v1beta1
  -for an endpoint   (default)    	kubectl run   --generator=deployment/v1beta1
  Deployment                      	kubectl run   --generator=apps/v1beta1  
  -for an endpoint   (recommended)	kubectl run   --generator=deployment/apps.v1beta1
  Job                             	kubectl run   --generator=job/v1        
  CronJob                         	kubectl run   --generator=batch/v1beta1 
  -for an endpoint   (default)    	kubectl run   --generator=cronjob/v1beta1
  CronJob                         	kubectl run   --generator=batch/v2alpha1
  -for an endpoint   (deprecated) 	kubectl run   --generator=cronjob/v2alpha1

如果未指定生成器标志，则其他标志会提示您使用特定的生成器。下表列出了强制您使用特定生成器的标志，具体取决于集群的版本：

注意：仅当您未指定任何标志时，这些标志才使用默认生成器。这意味着当您--generator与其他标志组合时，您稍后指定的生成器不会更改。例如，在集群v1.4中，如果您最初指定 --restart=Always，则创建部署; 如果以后规定--restart=Always 并且--generator=run/v1，创建一个复制控制器。这使您可以使用生成器固定特定行为，即使稍后更改默认生成器也是如此。

标志按以下顺序设置生成器：首先是--schedule标志，然后是--restart策略标志，最后是--generator标志。

要检查已创建的最终资源，请使用--dry-run 标志，该标志提供要提交给集群的对象。

kubectl apply

您可以使用它kubectl apply来创建或更新资源。有关使用kubectl apply更新资源的更多信息，请参阅Kubectl Book。

9.7    Docker用户的kubectl

第10章         工具
