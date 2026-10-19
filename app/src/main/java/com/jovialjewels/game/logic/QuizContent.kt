package com.jovialjewels.game.logic

/**
 * QUIZ CONTENT — "Circles, Balls & Spheres", 3 difficulty levels × 10 questions.
 * Replaceable: keep 3 levels × 10 questions; each question has ONE correct answer and three
 * wrong ones (4 options total). Options are shuffled at runtime, so always list the correct
 * answer in [QuizQuestion.correct]. Level ids are persisted as score keys — renaming an id
 * resets that level's best score.
 */
data class QuizQuestion(
    val text: String,
    val correct: String,
    val wrong: List<String>,
)

data class QuizCategory(
    val id: String,
    val title: String,
    val subtitle: String,
    /** Difficulty shown as 1..3 stars. */
    val stars: Int,
    val questions: List<QuizQuestion>,
)

object QuizContent {

    val categories: List<QuizCategory> = listOf(
        QuizCategory(
            id = "easy",
            title = "Easy",
            subtitle = "Warm-up juggling",
            stars = 1,
            questions = listOf(
                QuizQuestion("What 3D shape is a soap bubble floating in the air?", "Sphere", listOf("Cube", "Cone", "Pyramid")),
                QuizQuestion("What do we call the distance from a circle's center to its edge?", "Radius", listOf("Diameter", "Chord", "Arc")),
                QuizQuestion("The line across a circle through its center is the…", "Diameter", listOf("Radius", "Tangent", "Spiral")),
                QuizQuestion("The distance all the way around a circle is its…", "Circumference", listOf("Area", "Volume", "Angle")),
                QuizQuestion("How many degrees are there in a full circle?", "360", listOf("180", "90", "100")),
                QuizQuestion("Which ball has little dimples all over its surface?", "Golf ball", listOf("Tennis ball", "Bowling ball", "Beach ball")),
                QuizQuestion("Which planet is famous for its bright, wide rings?", "Saturn", listOf("Mars", "Venus", "Mercury")),
                QuizQuestion("How many wheels does a unicycle have?", "One", listOf("Two", "Three", "Four")),
                QuizQuestion("Which round toy climbs up and down a string?", "Yo-yo", listOf("Kite", "Frisbee", "Spinning top")),
                QuizQuestion("A crystal ball is a classic prop of a…", "Fortune teller", listOf("Baker", "Lifeguard", "Pilot")),
            ),
        ),
        QuizCategory(
            id = "medium",
            title = "Medium",
            subtitle = "Rolling with the circus",
            stars = 2,
            questions = listOf(
                QuizQuestion("What is the approximate value of pi (π)?", "3.14", listOf("2.72", "1.62", "4.13")),
                QuizQuestion("Which formula gives the area of a circle?", "πr²", listOf("2πr", "πd", "r² ÷ 2")),
                QuizQuestion("A straight line that touches a circle at exactly one point is a…", "Tangent", listOf("Secant", "Chord", "Radius")),
                QuizQuestion("A \"pizza slice\" of a circle between two radii is called a…", "Sector", listOf("Segment", "Chord", "Annulus")),
                QuizQuestion("The ring between two circles with the same center is an…", "Annulus", listOf("Ellipse", "Lune", "Oval")),
                QuizQuestion("The basic three-ball juggling pattern is called the…", "Cascade", listOf("Waterfall", "Tornado", "Pinwheel")),
                QuizQuestion("Which is the largest planet — the biggest sphere — in our Solar System?", "Jupiter", listOf("Saturn", "Neptune", "Earth")),
                QuizQuestion("How many panels does a classic black-and-white soccer ball have?", "32", listOf("24", "20", "36")),
                QuizQuestion("A circle's circumference divided by its diameter always equals…", "Pi (π)", listOf("2", "The radius", "The area")),
                QuizQuestion("What is the maximum weight of a regulation ten-pin bowling ball?", "16 pounds", listOf("10 pounds", "20 pounds", "25 pounds")),
            ),
        ),
        QuizCategory(
            id = "hard",
            title = "Hard",
            subtitle = "Royal geometry",
            stars = 3,
            questions = listOf(
                QuizQuestion("Which formula gives the volume of a sphere?", "4/3 πr³", listOf("4πr²", "πr²h", "2/3 πr³")),
                QuizQuestion("Which formula gives the surface area of a sphere?", "4πr²", listOf("2πr²", "πr³", "4/3 πr²")),
                QuizQuestion("Which shape has the smallest surface area for a given volume?", "Sphere", listOf("Cube", "Cylinder", "Tetrahedron")),
                QuizQuestion("A classic soccer ball is shaped like a truncated…", "Icosahedron", listOf("Octahedron", "Cube", "Tetrahedron")),
                QuizQuestion("How many pentagons are on a classic soccer ball?", "12", listOf("20", "10", "6")),
                QuizQuestion("An angle inscribed in a semicircle is always…", "90°", listOf("45°", "60°", "180°")),
                QuizQuestion("A central angle is how big compared with an inscribed angle on the same arc?", "Twice as big", listOf("Half as big", "The same", "Three times as big")),
                QuizQuestion("One radian is the angle whose arc length equals the…", "Radius", listOf("Diameter", "Circumference", "Area")),
                QuizQuestion("How many radians are in a full circle?", "2π", listOf("π", "360", "π/2")),
                QuizQuestion("A spinning ball curving through the air shows which effect?", "Magnus effect", listOf("Doppler effect", "Coriolis effect", "Hall effect")),
            ),
        ),
    )

    fun category(id: String?): QuizCategory? =
        if (id.isNullOrBlank()) null else categories.firstOrNull { it.id == id }
}
