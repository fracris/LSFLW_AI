% === Parametri ===
min_defense_ships(8).
expansion_threshold(10).
attack_threshold(15).

% === Sistemi strategici ===
strategic_system(S) :- production(S, P), P > 1.

system(s1). system(s2). system(s3).
connected(s1, s2). connected(s2, s3).

neighbor(S, T) :- connected(S, T).
neighbor(S, T) :- connected(T, S).

strategic_connection_count(S, N) :- system(S), #count { T : neighbor(S, T) } = N.


strategic_system(S) :- strategic_connection_count(S,N), N > 3.

% === Sistemi di frontiera ===
is_border_system(S) :-
    ai_player(AI), owner(S, AI),
    connected(S, T), not owner(T, AI).

is_border_system(S) :-
    ai_player(AI), owner(S, AI),
    connected(T, S), not owner(T, AI).

% === Sistemi interni ===
is_internal_system(S) :-
    ai_player(AI), owner(S, AI),
    not is_border_system(S).

% === Priorità del sistema ===
system_priority(S, 3) :- strategic_system(S), is_border_system(S).
system_priority(S, 2) :- strategic_system(S), is_internal_system(S).
system_priority(S, 2) :- is_border_system(S), not strategic_system(S).
system_priority(S, 1) :- is_internal_system(S), not strategic_system(S).

% === Navi necessarie per conquista ===
neutral_conquest_ships(S, Required) :-
    neutral(S), ships(S, N), Required = N + 2.

enemy_conquest_ships(S, Required) :-
    owner(S, O), ai_player(AI), O != AI,
    ships(S, N), Required = N + 5.

% === Strategia Difensiva ===


connected_bidirectional(X, Y) :- connected(X, Y).
connected_bidirectional(X, Y) :- connected(Y, X).


can_send_reinforcement(From, To) :-
    ai_player(AI),
    owner(From, AI), owner(To, AI),
    ships(From, FS), ships(To, TS),
    min_defense_ships(MD),
    FS > MD + 5, TS < MD,
    connected_bidirectional(From, To).

send_fleet(From, To, Ships) :-
    chosen_strategy(defensive),
    can_send_reinforcement(From, To),
    ships(From, FS), ships(To, TS),
    min_defense_ships(MD),
    Needed = MD - TS,
    Available = FS - MD,
    Needed <= Available,
    Ships = Needed.

send_fleet(From, To, Ships) :-
    chosen_strategy(defensive),
    can_send_reinforcement(From, To),
    ships(From, FS), ships(To, TS),
    min_defense_ships(MD),
    Needed = MD - TS,
    Available = FS - MD,
    Needed > Available,
    Ships = Available.

% === Strategia Espansione ===
potential_neutral_target(S, 3, R) :-
    neutral(S), strategic_system(S),
    connected(From, S), ai_player(AI), owner(From, AI),
    neutral_conquest_ships(S, R).

potential_neutral_target(S, 1, R) :-
    neutral(S), not strategic_system(S),
    connected(From, S), ai_player(AI), owner(From, AI),
    neutral_conquest_ships(S, R).

can_attack_from(From, To, Ships) :-
    ai_player(AI), owner(From, AI), neutral(To),
    connected(From, To),
    ships(From, FS), neutral_conquest_ships(To, R),
    min_defense_ships(MD),
    FS > R + MD, Ships = R.

can_attack_from(From, To, Ships) :-
    ai_player(AI), owner(From, AI), neutral(To),
    connected(To, From),
    ships(From, FS), neutral_conquest_ships(To, R),
    min_defense_ships(MD),
    FS > R + MD, Ships = R.


send_fleet(From, To, Ships) :-
    chosen_strategy(expansion),
    can_attack_from(From, To, Ships),
    potential_neutral_target(To, _, _).

% === Strategia Attacco ===
potential_enemy_target(S, 3, R) :-
    owner(S, O), ai_player(AI), O != AI,
    connected(From, S), owner(From, AI),
    enemy_conquest_ships(S, R),
    strategic_system(S).

potential_enemy_target(S, 1, R) :-
    owner(S, O), ai_player(AI), O != AI,
    connected(From, S), owner(From, AI),
    enemy_conquest_ships(S, R),
    not strategic_system(S).

can_attack_enemy_from(From, To, Ships) :-
    ai_player(AI), owner(From, AI), owner(To, O), O != AI,
    connected(From, To),
    ships(From, FS), enemy_conquest_ships(To, R),
    min_defense_ships(MD), FS > R + MD, Ships = R.

can_attack_enemy_from(From, To, Ships) :-
    ai_player(AI), owner(From, AI), owner(To, O), O != AI,
    connected(To, From),
    ships(From, FS), enemy_conquest_ships(To, R),
    min_defense_ships(MD), FS > R + MD, Ships = R.


send_fleet(From, To, Ships) :-
    chosen_strategy(aggressive),
    can_attack_enemy_from(From, To, Ships),
    potential_enemy_target(To, _, _).

% === Strategia Rinforzo ===
is_high_production_system(S) :- production(S, P), P > 1.
can_send_border_reinforcement(From, To) :-
    ai_player(AI), owner(From, AI), owner(To, AI),
    is_internal_system(From), is_border_system(To),
    ships(From, FS), FS > 15,
    connected(From, To).

can_send_border_reinforcement(From, To) :-
    ai_player(AI), owner(From, AI), owner(To, AI),
    is_internal_system(From), is_border_system(To),
    ships(From, FS), FS > 15,
    connected(To, From).


send_fleet(From, To, Ships) :-
    chosen_strategy(reinforcement),
    can_send_border_reinforcement(From, To),
    ships(From, FS), Ships = FS / 2.

% === Punteggio per preferenze (simulazione weak constraints) ===
score(2, From, To) :-
    send_fleet(From, To, _),
    potential_neutral_target(To, 3, _).

score(3, From, To) :-
    send_fleet(From, To, _),
    potential_enemy_target(To, 3, _).

% === Penalità per strategie eccessive ===
too_many_defense :-
    chosen_strategy(defensive),
    #count {F,T,S : send_fleet(F,T,S)} > 3.

too_many_expansion :-
    chosen_strategy(expansion),
    #count {F,T,S : send_fleet(F,T,S)} > 2.

too_many_attack :-
    chosen_strategy(aggressive),
    #count {F,T,S : send_fleet(F,T,S)} > 1.

too_many_reinforcement :-
    chosen_strategy(reinforcement),
    #count {F,T,S : send_fleet(F,T,S)} > 2.


chosen_strategy(reinforcement).

#show can_send_border_reinforcement/2.
#show send_fleet/3.