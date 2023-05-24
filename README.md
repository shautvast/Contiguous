**Design decisions**
* built for speed and efficiency (within java)
* uses SQLite storage with 1 exception: float is stored as f32
* needs jdk 9 (VarHandles)
* minimal reflection (once per creation of the list)

* The only class (for now): [ContiguousList](https://github.com/shautvast/Contiguous/blob/main/src/main/java/nl/sanderhautvast/contiguous/ContiguousList.java)

* Still in a very early stage
  * the code is working
  * but it remains to be seen if this is a good idea