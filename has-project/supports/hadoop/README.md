<!--
  Licensed to the Apache Software Foundation (ASF) under one
  or more contributor license agreements.  See the NOTICE file
  distributed with this work for additional information
  regarding copyright ownership.  The ASF licenses this file
  to you under the Apache License, Version 2.0 (the
  "License"); you may not use this file except in compliance
  with the License.  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing,
  software distributed under the License is distributed on an
  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  KIND, either express or implied.  See the License for the
  specific language governing permissions and limitations
  under the License.
-->

Enable Hadoop
================

## 1. Build Hadoop

### Apply the patch to hadoop-2.7.2 source code
```
git apply hadoop-2.7.2.patch
```

### Build Hadoop
```
mvn package -Pdist,native -Dtar -DskipTests -Dmaven.javadoc.skip=true -Dcontainer-executor.conf.dir=/etc/hadoop/conf
```

### Redeploy Hadoop

## 2. Distribute and configure Keytab files

a.  Deploy krb5.conf and has-client.conf

b. Create and distribute keytab files to the corresponding nodes.

c. Set permission of keytab files, restrict access permissions for any keytab files you create.

## 3. Update hadoop configuration files
 
### Update core-site.xml
add the following properties:
```
<property>
  <name>hadoop.security.authorization</name>
  <value>true</value>
</property>
<property>
  <name>hadoop.security.authentication</name>
  <value>kerberos</value>
</property>
<property>
   <name>hadoop.security.authentication.use.has</name>
   <value>true</value>
</property>
```

### Update hdfs-site.xml
add the following properties:
```
<!-- General HDFS security config -->
<property>
  <name>dfs.block.access.token.enable</name>
  <value>true</value>
</property>

<!-- NameNode security config -->
<property>
  <name>dfs.namenode.keytab.file</name>
  <value>/etc/hadoop/conf/hdfs.keytab</value>
</property>
<property>
  <name>dfs.namenode.kerberos.principal</name>
  <value>hdfs/_HOST@HADOOP.COM</value>
</property>
<property>
  <name>dfs.namenode.kerberos.internal.spnego.principal</name>
  <value>HTTP/_HOST@HADOOP.COM</value>
</property>
<property>
  <name>dfs.namenode.delegation.token.max-lifetime</name>
  <value>604800000</value>
  <description>The maximum lifetime in milliseconds for which a delegation token is valid.</description>
</property>

<!-- Secondary NameNode security config -->
<property>
  <name>dfs.secondary.namenode.keytab.file</name>
  <value>/etc/hadoop/conf/hdfs.keytab</value>
</property>
<property>
  <name>dfs.secondary.namenode.kerberos.principal</name>
  <value>hdfs/_HOST@HADOOP.COM</value>
</property>
<property>
  <name>dfs.secondary.namenode.kerberos.internal.spnego.principal</name>
  <value>HTTP/_HOST@HADOOP.COM</value>
</property>

<!-- DataNode security config -->
<property>
  <name>dfs.datanode.data.dir.perm</name>
  <value>700</value>
</property>
<property>
  <name>dfs.datanode.keytab.file</name>
  <value>/etc/hadoop/conf/hdfs.keytab</value>
</property>
<property>
  <name>dfs.datanode.kerberos.principal</name>
  <value>hdfs/_HOST@HADOOP.COM</value>
</property>

<!-- HTTPS config -->
<property>
  <name>dfs.http.policy</name>
  <value>HTTPS_ONLY</value>
</property>
<property>
  <name>dfs.data.transfer.protection</name>
  <value>integrity</value>
</property>
<property>
  <name>dfs.web.authentication.kerberos.keytab</name>
  <value>/etc/hadoop/conf/hdfs.keytab</value>
</property>
<property>
  <name>dfs.web.authentication.kerberos.principal</name>
  <value>HTTP/_HOST@HADOOP.COM</value>
</property>
```

### Configuration for HDFS HA

> For normal configuration, please look at [HDFS High Availability](https://hadoop.apache.org/docs/stable/hadoop-project-dist/hadoop-hdfs/HDFSHighAvailabilityWithNFS.html)

add the following properties in hdfs-site.xml:
```
<property>
  <name>dfs.journalnode.keytab.file</name>
  <value>/etc/hadoop/conf/hdfs.keytab</value>
</property>
<property>
  <name>dfs.journalnode.kerberos.principal</name>
  <value>hdfs/_HOST@HADOOP.COM</value>
</property>
<property>
  <name>dfs.journalnode.kerberos.internal.spnego.principal</name>
  <value>HTTP/_HOST@HADOOP.COM</value>
</property>
```

### Update yarn-site.xml
add the following properties:
```
<!-- ResourceManager security config -->
<property>
  <name>yarn.resourcemanager.keytab</name>
  <value>/etc/hadoop/conf/yarn.keytab</value>
</property>
<property>
  <name>yarn.resourcemanager.principal</name>
  <value>yarn/_HOST@HADOOP.COM</value>
</property>

<!-- NodeManager security config -->
<property>
  <name>yarn.nodemanager.keytab</name>
  <value>/etc/hadoop/conf/yarn.keytab</value>
</property>
<property>
  <name>yarn.nodemanager.principal</name> 
  <value>yarn/_HOST@HADOOP.COM</value>
</property>

<!-- HTTPS config -->
<property>
  <name>mapreduce.jobhistory.http.policy</name>
  <value>HTTPS_ONLY</value>
</property>

<!-- Container executor config -->
<property>
  <name>yarn.nodemanager.container-executor.class</name>
  <value>org.apache.hadoop.yarn.server.nodemanager.LinuxContainerExecutor</value>
</property>
<property>
  <name>yarn.nodemanager.linux-container-executor.group</name>
  <value>root</value>
</property>

<!-- Timeline service config, if timeline service enabled -->
<property>
  <name>yarn.timeline-service.principal</name>
  <value>yarn/_HOST@HADOOP.COM</value>
</property>

<property>
  <name>yarn.timeline-service.keytab</name>
  <value>/etc/hadoop/conf/yarn.keytab</value>
</property>

<property>
  <name>yarn.timeline-service.http-authentication.type</name>
  <value>kerberos</value>
</property>

<property>
  <name>yarn.timeline-service.http-authentication.kerberos.principal</name>
  <value>HTTP/_HOST@HADOOP.COM</value>
</property>

<property>
  <name>yarn.timeline-service.http-authentication.kerberos.keytab</name>
  <value>/etc/hadoop/conf/hdfs.keytab</value>
</property>

<!-- Proxy server config, if web proxy server enabled -->
<property>
  <name>yarn.web-proxy.keytab</name>
  <value>/etc/hadoop/conf/yarn.keytab</value>
</property>

<property>
  <name>yarn.web-proxy.principal</name>
  <value>yarn/_HOST@HADOOP.COM</value>
</property>
```

### Update mapred-site.xml
add the following properties:
```
<!-- MapReduce security config -->
<property>
  <name>mapreduce.jobhistory.keytab</name>
  <value>/etc/hadoop/conf/mapred.keytab</value>
</property>
<property>
  <name>mapreduce.jobhistory.principal</name>
  <value>mapred/_HOST@HADOOP.COM</value>
</property>
```

### Create and configure ssl-server.xml
```
cd $HADOOP_HOME
cp etc/hadoop/ssl-server.xml.example etc/hadoop/ssl-server.xml
```

Configure ssl-server.xml:
Please look at [How to deploy https](../../docs/deploy-https.md).

## 4. Configure container-executor

### Create and configure container-executor.cfg
`container-executor.cfg` locates in the path specified in the `mvn` build command. According to the example
`mvn package -Pdist,native -Dtar -DskipTests -Dmaven.javadoc.skip=true -Dcontainer-executor.conf.dir=/etc/hadoop/conf`
build command in this document, the path is `/etc/hadoop/conf/`.
Note that `container-executor.cfg` should be deployed to each node of cluster.

Example of container-executor.cfg:
```
#configured value of yarn.nodemanager.linux-container-executor.group
yarn.nodemanager.linux-container-executor.group=root
#comma separated list of users who can not run applications
banned.users=bin
#Prevent other super-users
min.user.id=0
#comma separated list of system users who CAN run applications
allowed.system.users=root,nobody,impala,hive,hdfs,yarn
```

Set permission:
```
mv container-executor.cfg /etc/hadoop/conf
// Container-executor.cfg should be read-only
chmod 400 container-executor.cfg
```

### Set permission of container-executor:
```
chmod 6050 container-executor
// Test whether configuration is correct
container-executor --checksetup
```
> Note that `container-executor` is in `$HADOOP_HOME/bin`.

## 5. Configure for Hadoop Client
There are two ways to enable HAS authentication for Hadoop client.

### a. Use HAS Plugin (MySQL plugin as example)

#### Make sure the plugin type is MySQL
Check `auth_type` value of `has-server.conf` and `has-client.conf`.

#### Create `has_user` table in MySQL Database
On HAS sever, create a table named `has_user`.
This table contains column named `user_name` and `pass_word` with type `VARCHAR`.

All the authenticate information of users stored in this table.

#### Configure environmental variables of HAS server
Add following environmental variables in the HAS sever:
```
mysqlUrl=jdbc:mysql://127.0.0.1:3306/NameOfHasDatabase
mysqlUser=MySQLUserNameForHas
mysqlPasswd=PasswordForDBUser
```

#### Configure environmental variables of Hadoop client
Add following environmental variables in the Hadoop client:
```
userName=HAS Client Name in has_user table
pass_word=HAS Client Password in has_user table
```

### b. Use legacy credential cache
Use `kinit` command to get credential cache.
```
knit -k -t path/to/any/keytab/file <pricipal_of_the_specified_keytab>
```

## 6. Setting up cross-realm for DistCp

### Setup cross realm trust between realms
Please look at [How to setup cross-realm](../../docs/cross-realm.md).

### Update core-site.xml

Set hadoop.security.auth_to_local parameter in both clusters, add the following properties:
```
<!-- Set up cross realm between A.HADOOP.COM and B.HADOOP.COM -->
<property>
    <name>hadoop.security.auth_to_local</name>
    <value> 
        RULE:[1:$1@$0](.*@A.HADOOP.COM)s/@A.HADOOP.COM///L
        RULE:[2:$1@$0](.*@A.HADOOP.COM)s/@A.HADOOP.COM///L
        RULE:[1:$1@$0](.*@B.HADOOP.COM)s/@B.HADOOP.COM///L
        RULE:[2:$1@$0](.*@B.HADOOP.COM)s/@B.HADOOP.COM///L
    </value>
</property>
```


Test the mapping:
```
hadoop org.apache.hadoop.security.HadoopKerberosName hdfs/localhost@A.HADOOP.COM
```

### Update hdfs-site.xml
add the following properties in client-side:
```
<!-- Control allowed realms to authenticate with -->
<property>
    <name>dfs.namenode.kerberos.principal.pattern</name>
    <value>*</value>
</property>
```

### Validate
Test trust is setup by running hdfs commands from A.HADOOP.COM to B.HADOOP.COM, run the following command on the node of A.HADOOP.COM cluster:
```
hdfs dfs ???ls hdfs://<NameNode_FQDN_for_B.HADOOP.COM_Cluster>:8020/
```

### Distcp between secure clusters

Run the distcp command:
```
hadoop distcp hdfs://<Cluster_A_URI> hdfs://<Cluster_B_URI>
```

### Distcp between secure and insecure clusters

Add the following properties in core-site.xml:
```
<property> 
  <name>ipc.client.fallback-to-simple-auth-allowed</name>
  <value>true</value>  
</property>
```

Or run the distcp command with security setting:
```
hadoop distcp -D ipc.client.fallback-to-simple-auth-allowed=true hdfs://<Cluster_A_URI> hdfs://<Cluster_B_URI>
```
