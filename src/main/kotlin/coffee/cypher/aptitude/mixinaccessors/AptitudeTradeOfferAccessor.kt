package coffee.cypher.aptitude.mixinaccessors

import coffee.cypher.aptitude.datamodel.AptitudeLevel

@Suppress("PropertyName")
interface AptitudeTradeOfferAccessor {
    var `aptitude$offeredByAptitudeLevel`: AptitudeLevel?
}
