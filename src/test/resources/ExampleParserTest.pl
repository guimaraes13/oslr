evidence(simplePredicate(a, b), true).
evidence(simplePredicate(a, b), false).
evidence(simpleProposition, true).
evidence(simpleProposition, false).

test(a, b)  +pred1(b, c)    -pred2(a, C, x, y)
test(a, b) -pred1(b, c) +prop
test(C, Z)+pred1(b, c)-pred2(a, C, x, y)-pred3(Z, x, Y)-prop
test(C, Z)  +pred1(b, c) -pred2(a, C, x, y)-pred3(Z, x, Y)  -prop
test(C, Z)  + pred1(b, c) - pred2(a, C, x, y)-pred3(Z, x, Y)  % -prop

% commented line
evidence(pred_constant("A - more complex - constant goes\t \"'here'\"!\n"),true).
evidence(pred_constant("A - more complex - constant goes\t \"'here'\"!\n"),false).
