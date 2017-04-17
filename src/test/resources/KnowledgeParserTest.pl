simplePredicate(a, b).
simpleProposition.

0.3::weightedPredicate(a, b, c, X, Y).
0.48::weightedProposition.


test(a, b) :- pred1(b, c), pred2(a, C, x, y).
test(a, b) :- pred1(b, c), prop.
test(C, Z) :- pred1(b, c), pred2(a, C, x, y), not pred3(Z, x, Y), not prop.

0.123::wTest :- another_pred(a, c, X).
0.342::test(a, b) :- pred1(b, c), pred2(a, C, x, y), not pred3(Z, x, Y), not prop.


0.2::bad.practice. 0.8::but.works.


% this is a commented line
featured(A, c) :- pred1(b, c), pred2(a, C, x, y). % , not pred3(Z, x, Y) {w}. commented
featured(A, c) :- pred1(b, c), pred2(a, C, x, y), not pred3(Z, x, Y) {w(X, Y)}.
featured(A, c) :- pred1(b, c), pred2(a, C, x, y), not pred3(Z, x, Y) {w, w(X, Y)}.
featured(A, B) :- {weight(A, B)}.

pred_constant("A - more complex - constant goes\t \"'here'\"!\n").
pred_constant("A", "ß", "Those are all constants, don't worry about the capital letter. < ;) ^^ >").
pred_constant("This is a comment sign (%) within a quoted constant").

head(A, B) :-
    body(A, C), body(C, B).
