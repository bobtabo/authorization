package schema

import (
	"entgo.io/ent"
	"entgo.io/ent/dialect/entsql"
	"entgo.io/ent/schema"
	"entgo.io/ent/schema/field"
	"time"
)

// Invitation holds the schema definition for the Invitation entity.
type Invitation struct {
	ent.Schema
}

func (Invitation) Annotations() []schema.Annotation {
	return []schema.Annotation{
		entsql.Annotation{Table: "invitations"},
	}
}

func (Invitation) Fields() []ent.Field {
	return []ent.Field{
		field.Uint("id").Positive().Immutable(),
		field.String("token"),
		field.Time("created_at").Default(time.Now),
		field.Uint("created_by").Optional().Nillable(),
		field.Time("updated_at").Default(time.Now),
		field.Uint("updated_by").Optional().Nillable(),
		field.Time("deleted_at").Optional().Nillable(),
		field.Uint("deleted_by").Optional().Nillable(),
		field.Int("version").Default(0),
	}
}

func (Invitation) Edges() []ent.Edge {
	return nil
}
