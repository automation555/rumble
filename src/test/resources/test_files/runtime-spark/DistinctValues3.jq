(:JIQS: ShouldCrash; ErrorCode="JNTY0004" :)
for $result in distinct-values(for $i in parallelize(1 to 100000, 10) return ({"foo":"bar"}))
order by $result
return $result
