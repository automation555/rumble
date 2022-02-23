(:JIQS: ShouldRun; Output="({ "Author" : "pregnanthollywood", "TotalScore" : -1 }, { "Author" : "uncannylizard", "TotalScore" : 0 }, { "Author" : "Mace55555", "TotalScore" : 1 }, { "Author" : "PeglegGecko", "TotalScore" : 1 }, { "Author" : "vhisic", "TotalScore" : 1 }, { "Author" : "asdjfweaiv", "TotalScore" : 1 }, { "Author" : "TimDisaster", "TotalScore" : 1 }, { "Author" : "kylionsfan", "TotalScore" : 1 }, { "Author" : "deephaven", "TotalScore" : 1 }, { "Author" : "politevelociraptor", "TotalScore" : 1 }, { "Author" : "billj457", "TotalScore" : 1 }, { "Author" : "MadagascarDifficulty", "TotalScore" : 1 }, { "Author" : "slicked9778", "TotalScore" : 1 }, { "Author" : "Crodface", "TotalScore" : 1 }, { "Author" : "MrRangerLP", "TotalScore" : 1 }, { "Author" : "Clomez", "TotalScore" : 1 }, { "Author" : "CarpeAeonem", "TotalScore" : 1 }, { "Author" : "Movepeck", "TotalScore" : 2 }, { "Author" : "Vamking12", "TotalScore" : 2 }, { "Author" : "gingerguitarx92x", "TotalScore" : 2 }, { "Author" : "Mastersimpson", "TotalScore" : 2 }, { "Author" : "marklar7", "TotalScore" : 2 }, { "Author" : "thebasedyeezus", "TotalScore" : 2 }, { "Author" : "highvoltorb", "TotalScore" : 2 }, { "Author" : "noitnemid", "TotalScore" : 2 }, { "Author" : "jaggazz", "TotalScore" : 2 }, { "Author" : "-purple-is-a-fruit-", "TotalScore" : 2 }, { "Author" : "FreeSoul789", "TotalScore" : 3 }, { "Author" : "RedCoatsForever", "TotalScore" : 3 }, { "Author" : "BigGupp1", "TotalScore" : 6 }, { "Author" : "[deleted]", "TotalScore" : 7 }, { "Author" : "submaRED", "TotalScore" : 7 }, { "Author" : "Meltingteeth", "TotalScore" : 10 }, { "Author" : "YoungModern", "TotalScore" : 14 }, { "Author" : "dewprisms", "TotalScore" : 17 })" :)
for $i in json-file("../../../queries/Reddit.json", 10)
let $author := $i.author, $score := $i.score
group by $author
let $totalScore := sum($score)
order by $totalScore
return {"Author": $author,
        "TotalScore": $totalScore}
