**Design decisions**
* built for speed and efficiency (within java)
* uses storage format borrowed from SQLite with 1 exception: float is stored as f32 (SQLite only uses f64)
* needs jdk 9 (VarHandles)
* minimal reflection (once per creation of the list)

* The only class (for now): [ContiguousList](https://github.com/shautvast/Contiguous/blob/main/lib/src/main/java/com/github/shautvast/contiguous/ContiguousList.java)

* Still in a very early stage
  * the code is working
  * but it remains to be seen if this is a good idea


-javaagent:/Users/Shautvast/dev/perfix/target/perfix-agent-0.1-SNAPSHOT.jar
-Dperfix.includes=nl.sanderhautvast.contiguous