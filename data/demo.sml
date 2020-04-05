package:glf.statemachine
class:Demonstration
name:test
===
A:=
B:= A.in0(a0)
C:= A.in1(a1)
A:= B.in0(a2)
C:= B.in1(a2)
A:= C(done)
