**Design decisions**
* built for speed and efficiency (within java)
* uses SQLite storage with 1 exception: float is stored as f32
* needs jdk 9 (VarHandles)
* minimal reflection (once per creation of the list)