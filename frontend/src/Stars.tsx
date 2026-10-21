// five stars where the gold part shows the score, 8.4 out of 10 fills 84 percent
export default function Stars({ score }: { score: number }) {
  const percent = (score / 10) * 100

  return (
    <span className="stars" aria-label={`${score} out of 10`}>
      ★★★★★
      {/* the gold stars lie on top of the grey ones and get cut off at the right width */}
      <span className="stars-fill" style={{ width: `${percent}%` }}>
        ★★★★★
      </span>
    </span>
  )
}