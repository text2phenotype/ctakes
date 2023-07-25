// Index|Test name|Text|IsCorrect
0|First Last|WARREN KUIPERS|True
1|First Middle Last|WARREN MID KUIPERS|True
2|First MiddleInitial Last|WARREN M. KUIPERS|True
3|Last First|KUIPERS WARREN|True
4|Last, First|KUIPERS, WARREN|True
5|Some text First Last|Provider name is WARREN KUIPERS|True
6|Some text First Middle Last|Provider name is WARREN MID KUIPERS|True
7|Some text First MiddleInitial Last|Provider name is WARREN M. KUIPERS|True
8|Some text Last First|Provider name is KUIPERS WARREN|True
9|Some text Last, First|Provider name is KUIPERS, WARREN|True
10|Some text First Last some text|Provider name is WARREN KUIPERS some text|True
11|Some text First Middle Last some text|Provider name is WARREN MID KUIPERS some text|True
12|Some text First MiddleInitial Last some text|Provider name is WARREN M. KUIPERS some text|True
13|Some text Last First some text|Provider name is KUIPERS WARREN some text|True
14|Some text Last, First some text|Provider name is KUIPERS, WARREN some text|True
15|Incorrect First Last|WARREN KUIPER|False
16|Incorrect First Middle Last|WARREN MID KUIPER|False
17|Incorrect First MiddleInitial Last|WARREN M. KUIPER|False
18|Incorrect Last First|KUIPER WARREN|False
19|Incorrect Last, First|KUIPER, WARREN|False
20|Incorrect name and double commas|KUIPER,, WARREN|False
21|Correct name and double commas|KUIPERS,, WARREN|False