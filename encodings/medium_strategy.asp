% ================================
% PROGRAMMA 2: ESECUZIONE STRATEGIA (VERSIONE SEMPLIFICATA)
% ================================


% Definizioni di base per i sistemi
my_system(S) :- system(S), owner(S,P), ai_player(P).
enemy_system(S) :- system(S), owner(S,P), not ai_player(P), P != 0.
neutral_system(S) :- system(S), neutral(S).

% Connessione non orientata semplificata
undirected_connected(M,S) :- connected(M,S).
undirected_connected(M,S) :- connected(S,M).

% Sistema di confine (vicino a nemici o neutrali)
border_system(S) :-
    my_system(S),
    undirected_connected(S,N),
    not my_system(N).

% Sistema centrale (non di confine)
central_system(S) :-
    my_system(S),
    not border_system(S).

% Sistema con alta produzione - semplificato
high_production_system(S) :-
    system(S),
    production(S,P),
    P >= 2.

% ===================================
% DEFINIZIONE DELLE POSSIBILI MOSSE (SEMPLIFICATE)
% ===================================

% --- Strategia Aggressiva ---
possible_move(aggressive, M, S, Ships, Score) :-
    my_system(M),
    neutral_system(S),
    undirected_connected(M,S),
    ships(M,MyShips),
    MyShips > 10,
    Ships = MyShips - 5,
    production(S,Prod),
    Score = Prod * 10.

possible_move(aggressive, M, S, Ships, Score) :-
    my_system(M),
    enemy_system(S),
    undirected_connected(M,S),
    ships(M,MyShips),
    ships(S,EnemyShips),
    MyShips > EnemyShips + 5,
    Ships = MyShips - 5,
    production(S,Prod),
    Score = Prod * 20.

% --- Strategia Difensiva (semplificata) ---
possible_move(defensive, M, S, Ships, Score) :-
    my_system(M),
    my_system(S),
    M != S,
    undirected_connected(M,S),
    border_system(S),
    ships(M,MyShips),
    MyShips > 15,
    Ships = MyShips / 2, % Semplificato a divisione intera
    Score = 100.

possible_move(defensive, M, S, Ships, Score) :-
    my_system(M),
    enemy_system(S),
    undirected_connected(M,S),
    ships(M,MyShips),
    ships(S,EnemyShips),
    MyShips > EnemyShips + (EnemyShips / 2),
    Ships = MyShips - 10,
    Score = 50.

% --- Strategia Tecnologica (semplificata) ---
possible_move(technological, M, S, Ships, Score) :-
    my_system(M),
    neutral_system(S),
    high_production_system(S),
    undirected_connected(M,S),
    ships(M,MyShips),
    MyShips > 20,
    Ships = MyShips - 10,
    production(S,Prod),
    Score = Prod * 30.

possible_move(technological, M, S, Ships, Score) :-
    my_system(M),
    enemy_system(S),
    high_production_system(S),
    undirected_connected(M,S),
    ships(M,MyShips),
    ships(S,EnemyShips),
    MyShips > 20,
    Ships = MyShips - 10,
    production(S,Prod),
    Score = Prod * 30.

% --- Strategia Espansione (semplificata) ---
possible_move(expansion, M, S, Ships, Score) :-
    my_system(M),
    neutral_system(S),
    undirected_connected(M,S),
    ships(M,MyShips),
    MyShips > 8,
    Ships = MyShips - 4,
    Score = 10.

% --- Strategia Consolidamento (semplificata) ---
possible_move(consolidation, M, S, Ships, Score) :-
    my_system(M),
    my_system(S),
    M != S,
    undirected_connected(M,S),
    ships(M,MyShips),
    ships(S,TargetShips),
    MyShips > TargetShips + 10,
    Ships = MyShips / 3, % Divisione intera semplificata
    Score = Ships.

% ================================
% SELEZIONE DEL MIGLIOR SEND_FLEET (SEMPLIFICATO)
% ================================

% Genera i candidati send_fleet in base alla strategia scelta
candidate_send_fleet(M, S, Ships, Score) :-
    chosen_strategy(Strategy),
    possible_move(Strategy, M, S, Ships, Score).

% Scegliamo esattamente una mossa strategica
{ strategic_send_fleet(M, S, Ships, Score) : candidate_send_fleet(M, S, Ships, Score) } = 1.

% Verifica che non si invii più navi di quelle disponibili
:- strategic_send_fleet(M, _, Ships, _),
   ships(M, Total),
   Ships > Total.

% ================================
% SEMPLIFICAZIONE DEI RINFORZI AUTOMATICI
% ================================

% Identifica i sistemi che necessitano rinforzo (solo frontiera)
needs_reinforcement(S) :-
    my_system(S),
    border_system(S).

% 1) Controllo invii già pianificati
has_send(M) :- strategic_send_fleet(M, _, _, _).

% 2) Calcola un terzo delle navi disponibili per M (intero)
available_third(M, Ships) :-
    ships(M, MyShips),
    MyShips > 20,
    Ships = MyShips / 3.

% 3) Potenziali donatori (solo sistemi centrali, senza invii già in corso)
potential_donor(M, Ships) :-
    my_system(M),
    central_system(M),
    not has_send(M),
    available_third(M, Ships).

% Genera candidati per rinforzo automatico (semplificato)
candidate_reinforcement(M, S, Ships) :-
    potential_donor(M, Available),
    Ships = Available / 2, % Divisione intera semplificata
    Ships > 5,
    my_system(S),
    border_system(S),
    undirected_connected(M, S),
    M != S.

% Seleziona rinforzi automatici (al massimo 2 per semplificare)
0 <= { auto_reinforcement(M, S, Ships) : candidate_reinforcement(M, S, Ships) } <=  2.

% Un sistema può essere solo donatore una volta
:- my_system(M),
   2 <= #count{S : auto_reinforcement(M, S, _)}.

% Un sistema può ricevere al massimo un rinforzo
:- my_system(S),
   2 <= #count{M : auto_reinforcement(M, S, _)}.

% Verifica che un sistema non doni più di quanto disponibile
:- my_system(M),
   ships(M, Total),
   #sum{Ships, S : auto_reinforcement(M, S, Ships)} = SumShips,
   SumShips > Total.

% ================================
% OUTPUT SEMPLIFICATO
% ================================

% Output finale combinato
send_fleet(M, S, Ships) :- strategic_send_fleet(M, S, Ships, _).
send_fleet(M, S, Ships) :- auto_reinforcement(M, S, Ships).

% Mostra solo i risultati principali
#show send_fleet/3.
