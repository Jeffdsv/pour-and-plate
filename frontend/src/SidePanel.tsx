import type { User } from './types'
import type { Person } from './people'
import InviteButton from './InviteButton'

type View = 'me' | 'everyone' | number

interface Props {
  me: User
  people: Person[]
  view: View
  open: boolean
  onSelect: (view: View) => void
  onClose: () => void
}

// first letter of max two words, so "Jeff Hansson" becomes "JH"
function initials(name: string) {
  return name
    .split(' ')
    .filter(Boolean)
    .slice(0, 2)
    .map((word) => word[0].toUpperCase())
    .join('')
}

function countsLine(person: Person | undefined) {
  if (!person) return ''
  return `${person.visitedCount} visited · ${person.wantCount} want to go`
}

interface RowProps {
  avatar: string
  name: string
  counts: string
  active: boolean
  onClick: () => void
}

// one clickable row in the list, avatar on the left and name with counts on the right
function PersonRow({ avatar, name, counts, active, onClick }: RowProps) {
  return (
    <li>
      <button className={active ? 'person active' : 'person'} onClick={onClick}>
        <span className="avatar">{avatar}</span>
        <span className="person-text">
          <span className="person-name">{name}</span>
          <span className="person-counts">{counts}</span>
        </span>
      </button>
    </li>
  )
}

// the list on the left where you pick whose map to look at
export default function SidePanel({ me, people, view, open, onSelect, onClose }: Props) {
  const myself = people.find((person) => person.id === me.id)
  const friends = people.filter((person) => person.id !== me.id)

  return (
    <aside className={open ? 'side-panel open' : 'side-panel'}>
      <div className="panel-header">
        <span className="panel-title">Maps</span>
        <button className="close-button-inline" onClick={onClose} aria-label="Close panel">
          ×
        </button>
      </div>

      <ul className="people">
        <PersonRow
          avatar={initials(me.displayName)}
          name="My map"
          counts={countsLine(myself)}
          active={view === 'me'}
          onClick={() => onSelect('me')}
        />
        <PersonRow
          avatar="★"
          name="Everyone"
          counts="All places we have saved"
          active={view === 'everyone'}
          onClick={() => onSelect('everyone')}
        />
        {friends.map((friend) => (
          <PersonRow
            key={friend.id}
            avatar={initials(friend.displayName)}
            name={friend.displayName}
            counts={countsLine(friend)}
            active={view === friend.id}
            onClick={() => onSelect(friend.id)}
          />
        ))}
      </ul>

      {/* only i can invite people, so only admins see this */}
      {me.role === 'ADMIN' && (
        <div className="panel-footer">
          <InviteButton />
        </div>
      )}
    </aside>
  )
}