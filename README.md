**Design decisions**

* built for speed and efficiency (within java)
* uses storage format borrowed from SQLite with 1 exception: float is stored as f32 (SQLite only uses f64)
* needs jdk 9 (VarHandles)
* minimal reflection (once per creation of the list)

* The only class (for
  now): [ContiguousList](https://github.com/shautvast/Contiguous/blob/main/lib/src/main/java/com/github/shautvast/contiguous/ContiguousList.java)

* Still in a very early stage
    * the code is working
    * but it remains to be seen if this is a good idea

**JMH Benchmark**

| Benchmark  | Mode | Cnt |        Score |        Error | Units |
|------------|------|----:|-------------:|-------------:|-------|
| classic    | avgt |   5 | 13312478.471 | ± 259931.663 | ns/op |
| contiguous | avgt |   5 | 13063782.511 | ± 451500.662 | ns/op |

--> pretty much on par despite all the overhead for inspecting the list elements
--> will try to squeeze out more performance

```
[ec2-user@ip-172-31-22-215 Contiguous]$ lscpu
Architecture:          x86_64
CPU op-mode(s):        32-bit, 64-bit
Address sizes:         46 bits physical, 48 bits virtual
Byte Order:            Little Endian
CPU(s):                1
On-line CPU(s) list:   0
Vendor ID:             GenuineIntel
Model name:            Intel(R) Xeon(R) CPU E5-2676 v3 @ 2.40GHz
CPU family:            6
Model:                 63
Thread(s) per core:    1
Core(s) per socket:    1
Socket(s):             1
Stepping:              2
BogoMIPS:              4800.04
```

